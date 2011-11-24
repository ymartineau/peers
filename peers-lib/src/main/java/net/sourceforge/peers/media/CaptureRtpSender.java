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
    
    Copyright 2007, 2008, 2009, 2010, 2011 Yohann Martineau 
*/

package net.sourceforge.peers.media;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.rtp.RFC3551;
import net.sourceforge.peers.rtp.RtpSession;
import net.sourceforge.peers.sdp.Codec;



public class CaptureRtpSender {

    public static final int PIPE_SIZE = 4096;

    private RtpSession rtpSession;
    private Capture capture;
    private Encoder encoder;
    private RtpSender rtpSender;

    public CaptureRtpSender(RtpSession rtpSession, SoundManager soundManager,
            boolean mediaDebug, Codec codec, Logger logger, String peersHome)
            throws IOException {
        super();
        this.rtpSession = rtpSession;
        // the use of PipedInputStream and PipedOutputStream in Capture,
        // Encoder and RtpSender imposes a synchronization point at the
        // end of life of those threads to a void read end dead exceptions
        CountDownLatch latch = new CountDownLatch(3);
        PipedOutputStream rawDataOutput = new PipedOutputStream();
        PipedInputStream rawDataInput;
        try {
            rawDataInput = new PipedInputStream(rawDataOutput, PIPE_SIZE);
        } catch (IOException e) {
            logger.error("input/output error", e);
            return;
        }
        
        PipedOutputStream encodedDataOutput = new PipedOutputStream();
        PipedInputStream encodedDataInput;
        try {
            encodedDataInput = new PipedInputStream(encodedDataOutput,
                    PIPE_SIZE);
        } catch (IOException e) {
            logger.error("input/output error");
            return;
        }
        capture = new Capture(rawDataOutput, soundManager, logger, latch);
        switch (codec.getPayloadType()) {
        case RFC3551.PAYLOAD_TYPE_PCMU:
            encoder = new PcmuEncoder(rawDataInput, encodedDataOutput,
                    mediaDebug, logger, peersHome, latch);
            break;
        case RFC3551.PAYLOAD_TYPE_PCMA:
            encoder = new PcmaEncoder(rawDataInput, encodedDataOutput,
                    mediaDebug, logger, peersHome, latch);
            break;
        default:
            throw new RuntimeException("unknown payload type");
        }
        rtpSender = new RtpSender(encodedDataInput, rtpSession, mediaDebug,
                codec, logger, peersHome, latch);
    }

    public void start() throws IOException {
        
        capture.setStopped(false);
        encoder.setStopped(false);
        rtpSender.setStopped(false);
        
        Thread captureThread = new Thread(capture);
        Thread encoderThread = new Thread(encoder);
        Thread rtpSenderThread = new Thread(rtpSender);
        
        captureThread.start();
        encoderThread.start();
        rtpSenderThread.start();
        
    }

    public void stop() {
        if (capture != null) {
            capture.setStopped(true);
        }
        if (encoder != null) {
            encoder.setStopped(true);
        }
        if (rtpSender != null) {
            rtpSender.setStopped(true);
        }
    }

    public synchronized RtpSession getRtpSession() {
        return rtpSession;
    }

    public RtpSender getRtpSender() {
        return rtpSender;
    }

}
