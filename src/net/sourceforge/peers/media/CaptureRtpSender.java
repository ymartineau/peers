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


public class CaptureRtpSender {

    private RtpManager rtpManager;

    private RtpSession rtpSession;
    private UlawStream ulawStream;

    public CaptureRtpSender(String localAddress, int localPort,
            String remoteAddress, int remotePort)
            throws IOException {
        super();
        rtpManager = new RtpManager(localAddress);
        rtpSession = rtpManager.createRtpSession(localPort, remoteAddress,
                remotePort);
        this.ulawStream = new UlawStream(rtpSession);
    }

    public void start() throws IOException {
        ulawStream.setStopped(false);
        new Thread(ulawStream).start();
    }

    public void stop() {
        if (ulawStream != null) {
            ulawStream.setStopped(true);
        }
        if (rtpSession != null) {
            rtpSession.shutDown();
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("usage: java ... "
                    + "<local_ip> <local_port> <remote_ip> <remote_port>");
            return;
        }
        CaptureRtpSender sender;
        try {
            sender = new CaptureRtpSender(args[0], Integer.parseInt(args[1]),
                    args[2], Integer.parseInt(args[3]));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            sender.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }
        sender.stop();
    }

    public synchronized RtpSession getRtpSession() {
        return rtpSession;
    }

    
}
