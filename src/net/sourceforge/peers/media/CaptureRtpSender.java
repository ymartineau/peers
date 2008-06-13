/*
    This file is part of Peers.

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
    
    Copyright 2007, 2008 Yohann Martineau 
*/

package net.sourceforge.peers.media;

import gov.nist.jrtp.RtpManager;
import gov.nist.jrtp.RtpSession;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;



public class CaptureRtpSender {

    private RtpManager rtpManager;

    private RtpSession rtpSession;
    private Capture capture;
    private Encoder encoder;
    private RtpSender rtpSender;

    public CaptureRtpSender(String localAddress, int localPort,
            String remoteAddress, int remotePort)
            throws IOException {
        super();
        rtpManager = new RtpManager(localAddress);
        rtpSession = rtpManager.createRtpSession(localPort, remoteAddress,
                remotePort);
        PipedOutputStream rawDataOutput = new PipedOutputStream();
        PipedInputStream rawDataInput;
        try {
            rawDataInput = new PipedInputStream(rawDataOutput);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        
        PipedOutputStream encodedDataOutput = new PipedOutputStream();
        PipedInputStream encodedDataInput;
        try {
            encodedDataInput = new PipedInputStream(encodedDataOutput);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        
        capture = new Capture(rawDataOutput);
        encoder = new Encoder(rawDataInput, encodedDataOutput);
        rtpSender = new RtpSender(encodedDataInput, rtpSession);
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
        if (rtpSender != null) {
            rtpSender.setStopped(true);
        }
        if (encoder != null) {
            encoder.setStopped(true);
        }
        if (capture != null) {
            capture.setStopped(true);
        }
        if (rtpSession != null) {
            rtpSession.shutDown();
        }
    }
 
    public synchronized RtpSession getRtpSession() {
        return rtpSession;
    }

    
}
