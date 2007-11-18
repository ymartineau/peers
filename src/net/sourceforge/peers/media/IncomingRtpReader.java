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

    private RtpSession rtpSession;
    private SourceDataLine line;

    public IncomingRtpReader(RtpSession rtpSession) throws IOException { 
        super();
        this.rtpSession = rtpSession;
        rtpSession.addRtpListener(this);
    }
    
    public void start() throws IOException, RtpException {
        AudioFormat format = new AudioFormat((float)8000, 16, 1, true, false);
        
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
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
        rtpSession.shutDown();
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
        RtpPacket rtpPacket = rtpEvent.getRtpPacket();

        byte[] data = new byte[rtpPacket.getData().length - 12];
        //remove RTP header: rtpPacket.getData() returns raw
        //packet bytes (with headers)
        System.arraycopy(rtpPacket.getData(), 12, data, 0,
                rtpPacket.getData().length - 12);

		byte[] rawBuf = new byte[data.length * 2];
		for (int i = 0; i < data.length; ++i) {
			short decoded = AudioUlawEncodeDecode02.decode(data[i]);
			rawBuf[2 * i] = (byte)(decoded & 0xFF);
			rawBuf[2 * i + 1] = (byte)(decoded >>> 8);
		}
		line.write(rawBuf, 0, rawBuf.length);
        
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
