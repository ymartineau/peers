package net.sourceforge.peers.media;

import gov.nist.jrtp.RtpErrorEvent;
import gov.nist.jrtp.RtpException;
import gov.nist.jrtp.RtpListener;
import gov.nist.jrtp.RtpPacket;
import gov.nist.jrtp.RtpPacketEvent;
import gov.nist.jrtp.RtpSession;
import gov.nist.jrtp.RtpStatusEvent;
import gov.nist.jrtp.RtpTimeoutEvent;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class IncomingRtpReader implements RtpListener {

    //private RtpManager rtpManager;
    
    private RtpSession rtpSession;
    private SourceDataLine line;
    private byte[] buffer;
    private int index;
    //private UlawStream ulawStream;
    
//    public IncomingRtpReader(String localAddress, int localPort,
//            String remoteAddress, int remotePort) throws IOException {
    public IncomingRtpReader(RtpSession rtpSession) throws IOException { 
        super();
        //rtpManager = new RtpManager(localAddress);
        //rtpSession = rtpManager.createRtpSession(localPort, remoteAddress, remotePort);
        this.rtpSession = rtpSession;
        rtpSession.addRtpListener(this);
        //this.ulawStream = new UlawStream(rtpSession);
    }
    
    public void start() throws IOException, RtpException {
        AudioFormat format = new AudioFormat((float)8000, 16, 1, true, false);
        
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            buffer = new byte[1024];
            index = 0;
        } catch (LineUnavailableException ex) {
            // Handle the error ... 
            ex.printStackTrace();
            return;
        }
        line.start();
        
        rtpSession.receiveRTPPackets();
    }
    
    public void stop() {
        rtpSession.stopRtpPacketReceiver();
        line.drain();
        line.stop();
        line.close();
        line = null;
    }

    public void handleRtpErrorEvent(RtpErrorEvent arg0) {
        // TODO Auto-generated method stub
        System.out.println("IncomingRtpReader.handleRtpErrorEvent()");
    }

    public void handleRtpPacketEvent(RtpPacketEvent rtpEvent) {
        // TODO Auto-generated method stub
//      Print the remote IP address and port
        RtpSession rtpSession = (RtpSession) rtpEvent.getSource();

        RtpPacket rtpPacket = rtpEvent.getRtpPacket();

        
        // TODO open javasound source dataline for sound output
        
        ////////////////////////////////////////////////////////////
        // added to play incoming RTP packets
        //TODO move those initalization values elsewhere
//        AudioUlawEncodeDecode02.value = 0;
//        AudioUlawEncodeDecode02.increment = 1;
//        AudioUlawEncodeDecode02.limit = 4;
        byte[] data = rtpPacket.getData();
        //byte[] pcmData = new byte[data.length * 2];
        
        //TODO if data.length > buffer.length
        //           bla bla
        //     else
        //           bla bla
        
        int i = 0;
        short value;
        while (i < data.length && index + 2 * i < buffer.length) {
            value = AudioUlawEncodeDecode02.decode(data[i]);
            buffer[index + 2 * i] = (byte)(value & 0xFF);
            buffer[index + 2 * i + 1] = (byte)(value >>> 8);
            ++i;
        }
        if (index + 2 * i >= buffer.length) {
            line.write(buffer, 0, buffer.length);
            buffer = new byte[buffer.length];
            index = 0;
            //TODO copy remaining data in buffer and manage case where remaining data length > buffer length
//            int j = 0;
//            while (j < data.length - i && j * 2 < buffer.length) {
//                value = AudioUlawEncodeDecode02.decode(data[i]);
//                buffer[2 * j] = (byte)(value & 0xFF);
//                buffer[2 * j + 1] = (byte)(value >>> 8);
//                ++j;
//            }
            index = 2 * (data.length - i);
        } else {
            index += i * 2;
        }
        //for (int i = 0; i < data.length; ++i) {
        //    short value = AudioUlawEncodeDecode02.decode(data[i]);
//            System.out.println("value = " + value + "\n"
//                    + "(byte)(value & 0xFF) = " + (byte)(value & 0xFF) + "\n"
//                    + "(byte)(value >>> 8) = " + (byte)(value >>> 8));
            //pcmData[2 * i] = (byte)(value & 0xFF);
            //pcmData[2 * i + 1] = (byte)(value >>> 8);
            
            //Warning: we suppose that buffer size is higher than 2*(data size)
        //    if (index + 2 * i >= buffer.length) {
        //        break;
        //    }
        //    buffer[index + 2 * i] = (byte)(value & 0xFF);
        //    buffer[index + 2 * i + 1] = (byte)(value >>> 8);
        //}
        
//        index += 2 * data.length;
//        
//        if (index >= buffer.length) {
//            line.write(buffer, 0, buffer.length);
//            index = 0;
//        }
//        System.out.println("data.length = " + data.length);
//        System.out.println("pcmData.length = " + pcmData.length);
        //warning: check that buffer size is not too high
        //TODO sound decoded correctly, but jitters appear permanently
        //line.write(pcmData, 0, pcmData.length);
        
        ////////////////////////////////////////////////////////////
        
        
        // informative: code used to encode data
        
        // Read the next chunk of data from the TargetDataLine.
        //numBytesRead = line.read(data, 0, data.length);
        // Save this chunk of data.
        
        
//        byte[] ulawData = new byte[data.length/2];
//        AudioUlawEncodeDecode02.value = 0;
//        AudioUlawEncodeDecode02.increment = 1;
//        AudioUlawEncodeDecode02.limit = 4;
//        for (int i = 0; i < numBytesRead; i += 2) {
//            //TODO data length odd
//            //TODO manage data endianess
//            short value = (short)data[i];
//            value += 256 * (short)data[i+1];
//            ulawData[i/2] = AudioUlawEncodeDecode02.encode(value);
//        }
//        
//        baos.write(ulawData, 0, ulawData.length);
        ////////////////////////////////////////////////////////////
        
        
        
        System.out.println("Received RTP packet #" + rtpPacket.getSN() + "\n");

        System.out.println("---------------\n[TestApplication] RTP Data:");
        System.out.println("[TestApplication] Received V: " + rtpPacket.getV());
        System.out.println("[TestApplication] Received P: " + rtpPacket.getP());
        System.out.println("[TestApplication] Received X: " + rtpPacket.getX());
        System.out.println("[TestApplication] Received CC: " + rtpPacket.getCC());
        System.out.println("[TestApplication] Received M: " + rtpPacket.getM());
        System.out.println("[TestApplication] Received PT: " + rtpPacket.getPT());
        System.out.println("[TestApplication] Received SN: " + rtpPacket.getSN());
        System.out.println("[TestApplication] Received TS: " + rtpPacket.getTS());
        System.out.println("[TestApplication] Received SSRC: " + rtpPacket.getSSRC());
        System.out.println("[TestApplication] Received Payload size: " + rtpPacket.getPayloadLength());
    }

    public void handleRtpStatusEvent(RtpStatusEvent arg0) {
        // TODO Auto-generated method stub
        System.out.println("IncomingRtpReader.handleRtpStatusEvent()");
    }

    public void handleRtpTimeoutEvent(RtpTimeoutEvent arg0) {
        // TODO Auto-generated method stub
        System.out.println("IncomingRtpReader.handleRtpTimeoutEvent()");
    }
}
