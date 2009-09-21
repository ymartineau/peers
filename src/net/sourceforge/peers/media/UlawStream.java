/*
    This file is part of Peers, a java SIP softphone.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    Copyright 2007, 2008, 2009 Yohann Martineau 
*/

package net.sourceforge.peers.media;

import gov.nist.jrtp.RtpException;
import gov.nist.jrtp.RtpPacket;
import gov.nist.jrtp.RtpSession;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import net.sourceforge.peers.Logger;


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
    
    private TargetDataLine line;
    
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

        AudioFormat format = new AudioFormat((float)8000, 16, 1, true, false);
        //G.711
//            AudioFormat format = new AudioFormat(AudioFormat.Encoding.ULAW, 8000, 16, 1,
//                    16, 8000, true);
        
        //TODO code cleaning: targetEncoding is only employed to display info...
        AudioFormat.Encoding targetEncoding = AudioFormat.Encoding.ULAW;
        
        Logger.debug("target encodings for " + format.getEncoding());
        AudioFormat.Encoding[] encodings =
            AudioSystem.getTargetEncodings(format.getEncoding());
        for (AudioFormat.Encoding encoding : encodings) {
            Logger.debug(encoding.toString());
        }
        
        
        AudioFormat[] formats = AudioSystem.getTargetFormats(targetEncoding, format);
        AudioFormat targetFormat = null;
        if (formats.length >= 1) {
            targetFormat = formats[0];
        }
        if (targetFormat == null) {
            Logger.error("no audio format found");
            return;
        }
        
        
        
//            AudioInputStream ulawAIS = AudioSystem.getAudioInputStream(targetFormat,
//                    sourceStream);
        
        Logger.debug("target formats");
        for (AudioFormat audioFormat : formats) {
            Logger.debug(audioFormat.toString());
        }
        
        
        
//            if (AudioSystem.isConversionSupported(AudioFormat.Encoding.ULAW,
//                    new AudioFormat((float)8000, 16, 1, true, false))) {
//                logger.debug("conversion ok");
//            } else {
//                logger.debug("conversion unavailable");
//            }
        
        
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        
        



        
        
        try {

            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            
            
        } catch (LineUnavailableException e) {
            Logger.error("line unavailable", e);
            return;
        }
        line.start();
        
        //separate capture in another thread
        
        Thread thread = new Thread(new CaptureRunnable());
        thread.start();
    }

    public synchronized void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
    
    /**
     * RTP Payload size of packets sent by wengophone: 160 bytes
     * RTP Payload size of packets sent by wengophone: 512 bytes, then 34 bytes
     */
    class CaptureRunnable implements Runnable {
        public void run() {

            int numBytesRead;
            byte[] data = new byte[line.getBufferSize() / 5];

            long startupTime = System.currentTimeMillis();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // Set up a test RTP packet
            RtpPacket rtpPacket = new RtpPacket();
            rtpPacket.setV(2);
//              rtpPacket.setP(1);
//              rtpPacket.setX(1);
//              rtpPacket.setCC(1);
//              rtpPacket.setM(1);
            rtpPacket.setPT(0);
//              rtpPacket.setTS(System.currentTimeMillis());
            rtpPacket.setSSRC(1);
//            long captureTime = System.currentTimeMillis();
            while (!stopped) {
                
//                long previousCaptureTime = captureTime;
//                captureTime = System.currentTimeMillis();
//                long captureInterval = captureTime - previousCaptureTime;
                
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
                
                
                byte[] buf = baos.toByteArray();
                int maxSize = 160;//RtpPacket.MAX_PAYLOAD_BUFFER_SIZE;
//                rtpPacket.setTS(System.currentTimeMillis() - startupTime);
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
                        rtpPacket.setTS(System.currentTimeMillis() - startupTime);
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
                            } catch (InterruptedException ie) {
                                Logger.error("Thread interrupted", ie);
                            }
                        } catch (RtpException re) {
                            Logger.error("RTP error", re);
                        } catch (IOException ioe) {
                            Logger.error("input/output error", ioe);
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
            
            Logger.debug("streaming finished");
        }
    }
    
}