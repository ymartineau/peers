/*
    This file is part of Peers.

    Peers is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Peers is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    
    Copyright 2007 Yohann Martineau 
*/

package net.sourceforge.peers.media;

import gov.nist.jrtp.RtpException;
import gov.nist.jrtp.RtpPacket;
import gov.nist.jrtp.RtpSession;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;


/**
 * This class implements the wrapper for reading data from a file and streaming
 * it to the RTP manager.
 * 
 * @author steveq@nist.gov
 * @version $Revision: 1.3 $, $Date: 2007/06/03 18:33:23 $
 * @since 1.5
 */
public class UlawStream implements Runnable {

	/***************************************************************************
	 * Variables
	 **************************************************************************/

    private RtpSession rtpSession;
    private boolean stopped = false;
    
	/** The rate in miliseconds at which packets are sent.*/
	private static long RATE = 20;

	/***************************************************************************
	 * Constructors
	 **************************************************************************/

	/**
	 * Construct a wrapper for streaming file data.
	 */
	public UlawStream(RtpSession rtpSession) {
        this.rtpSession = rtpSession;
	}

	/***************************************************************************
	 * Methods
	 **************************************************************************/

	/**
	 * Restart reading on a waiting stream.
	 */
	public void restart() {

		synchronized (this) {

			this.notify();

		}
	}

    /**
     * Run this object.
     */
    public void run() {

        //int bufferSize = RtpPacket.MAX_PAYLOAD_BUFFER_SIZE;
        //byte[] buffer = new byte[bufferSize];

        try {

            AudioFormat format = new AudioFormat((float)8000, 16, 1, true, false);
            //G.711
//            AudioFormat format = new AudioFormat(AudioFormat.Encoding.ULAW, 8000, 16, 1,
//                    16, 8000, true);
            
            //TODO code cleaning: targetEncoding is only employed to display info...
            AudioFormat.Encoding targetEncoding = AudioFormat.Encoding.ULAW;
            
            
            System.out.println("target encodings for " + format.getEncoding());
            AudioFormat.Encoding[] encodings =
                AudioSystem.getTargetEncodings(format.getEncoding());
            for (AudioFormat.Encoding encoding : encodings) {
                System.out.println(encoding);
            }
            
            
            AudioFormat[] formats = AudioSystem.getTargetFormats(targetEncoding, format);
            AudioFormat targetFormat = null;
            if (formats.length >= 1) {
                targetFormat = formats[0];
            }
            if (targetFormat == null) {
                System.err.println("no audio format found");
                return;
            }
            
            
            
//            AudioInputStream ulawAIS = AudioSystem.getAudioInputStream(targetFormat,
//                    sourceStream);
            
            System.out.println("target formats");
            for (AudioFormat audioFormat : formats) {
                System.out.println(audioFormat);
            }
            
            
            
//            if (AudioSystem.isConversionSupported(AudioFormat.Encoding.ULAW,
//                    new AudioFormat((float)8000, 16, 1, true, false))) {
//                System.out.println("conversion ok");
//            } else {
//                System.out.println("conversion unavailable");
//            }
            
            
            TargetDataLine line;
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            
            

            // Set up a test RTP packet
            RtpPacket rtpPacket = new RtpPacket();
            rtpPacket.setV(2);
//          rtpPacket.setP(1);
//          rtpPacket.setX(1);
//          rtpPacket.setCC(1);
//          rtpPacket.setM(1);
            rtpPacket.setPT(0);
//          rtpPacket.setTS(System.currentTimeMillis());
            rtpPacket.setSSRC(1);

            long startupTime = System.currentTimeMillis();
            
            try {

                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                
                
            } catch (LineUnavailableException ex) {
                // Handle the error ... 
                ex.printStackTrace();
                return;
            }
            line.start();
            int numBytesRead;
            byte[] data = new byte[line.getBufferSize() / 5];

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            while (!stopped) {
                // Read the next chunk of data from the TargetDataLine.
                numBytesRead = line.read(data, 0, data.length);
                // Save this chunk of data.
                
                
                byte[] ulawData = new byte[data.length/2];
                AudioUlawEncodeDecode02.value = 0;
                AudioUlawEncodeDecode02.increment = 1;
                AudioUlawEncodeDecode02.limit = 4;
                for (int i = 0; i < numBytesRead; i += 2) {
                    //TODO data length odd
                    //TODO manage data endianess
                    short value = (short)data[i];
                    value += 256 * (short)data[i+1];
                    ulawData[i/2] = AudioUlawEncodeDecode02.encode(value);
                }
                
                baos.write(ulawData, 0, ulawData.length);
                
                
                
                
                ///////experimental
//                InputStream in = new ByteArrayInputStream(baos.toByteArray());
//                AudioInputStream ais = null;
//                try {
//                    ais = AudioSystem.getAudioInputStream(in);
//                } catch (UnsupportedAudioFileException e1) {
//                    // TODO Auto-generated catch block
//                    e1.printStackTrace();
//                }
//                AudioInputStream ulawAIS = AudioSystem.getAudioInputStream(
//                        targetFormat, ais);
                /////////////////////
                
                
                byte[] buf = baos.toByteArray();
                int maxSize = RtpPacket.MAX_PAYLOAD_BUFFER_SIZE;
                rtpPacket.setTS(System.currentTimeMillis() - startupTime);
                if (buf.length > maxSize) {
                    int index = 0;
                    while (index < buf.length) {
                        byte[] smallerBuf;
                        if (index + maxSize > buf.length) {
                            smallerBuf = new byte[buf.length - index];
                        } else {
                            smallerBuf  = new byte[maxSize];
                        }
                        System.arraycopy(buf, index, smallerBuf, 0,
                                smallerBuf.length);
                        index += maxSize;
                        rtpPacket.setPayload(smallerBuf, smallerBuf.length);
                        try {

                            rtpSession.sendRtpPacket(rtpPacket);
                            
                            /* Note that an application is responsible for the timing
                               of packet delays between each outgoing packet.  Here,
                               we set to an arbitrary value = 10ms.  A better method
                               (not shown) is to check elapsed time between the 
                               sending of each packet to reduce jitter.
                            */
                            try {
                                Thread.sleep(RATE);
                                
                            } catch (Exception e) {
                                
                                e.printStackTrace();
                            }

                        } catch (RtpException re) {

                            re.printStackTrace();

                        }
                    }
                }
                
                
                baos.reset();
                
            } 
            
            //TODO close cleanly (target data) line
            line.drain();
            line.stop();
            line.close();
            line = null;
            
            
//            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            // TODO 22,050 Hz => 8,000 Hz
//            AudioFormat outDataFormat = new AudioFormat((float)8000.0, 8, 1,
//                    true, false);
//            if (!AudioSystem.isConversionSupported(outDataFormat,
//                    ais.getFormat())) {
//                System.err.println("conversion not supported");
//                return;
//            }
//            AudioInputStream lowResAis = AudioSystem.getAudioInputStream(
//                    outDataFormat, ais);
            
//            AudioInputStream ulawAis = AudioSystem.getAudioInputStream(
//                    AudioFormat.Encoding.ULAW, ais/*lowResAis*/);
//            
//            
//            
//            
//            AudioSystem.write(ulawAis, AudioFileFormat.Type.WAVE, baos);
//            rtpPacket.setTS(System.currentTimeMillis() - startupTime);
//            byte[] buf = baos.toByteArray();
//            int maxSize = RtpPacket.MAX_PAYLOAD_BUFFER_SIZE;
//            if (buf.length > maxSize) {
//                int index = 0;
//                while (index < buf.length) {
//                    byte[] smallerBuf;
//                    if (index + maxSize > buf.length) {
//                        smallerBuf = new byte[buf.length - index];
//                    } else {
//                        smallerBuf  = new byte[maxSize];
//                    }
//                    System.arraycopy(buf, index, smallerBuf, 0,
//                            smallerBuf.length);
//                    index += maxSize;
//                    rtpPacket.setPayload(smallerBuf, smallerBuf.length);
//                    try {
//
//                        rtpSession.sendRtpPacket(rtpPacket);
//                        
//                        /* Note that an application is responsible for the timing
//                           of packet delays between each outgoing packet.  Here,
//                           we set to an arbitrary value = 10ms.  A better method
//                           (not shown) is to check elapsed time between the 
//                           sending of each packet to reduce jitter.
//                        */
//                        try {
//                            Thread.sleep(RATE);
//                            
//                        } catch (Exception e) {
//                            
//                            e.printStackTrace();
//                        }
//
//                    } catch (RtpException re) {
//
//                        re.printStackTrace();
//
//                    }
//                }
//            }
//            baos.close();
//                
//            
//            fileInputStream.close();
            
            System.out.println("done");

        } catch (FileNotFoundException fnfe) {

            fnfe.printStackTrace();

        } catch (IOException ioe) {

            ioe.printStackTrace();

        }
    }

    public synchronized void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
    
    
}