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
    
    Copyright 2008, 2009 Yohann Martineau 
*/

package net.sourceforge.peers.media;

import gov.nist.jrtp.RtpException;
import gov.nist.jrtp.RtpPacket;
import gov.nist.jrtp.RtpSession;

import java.io.IOException;
import java.io.PipedInputStream;
import java.net.UnknownHostException;

import net.sourceforge.peers.Logger;

public class RtpSender implements Runnable {

    private PipedInputStream encodedData;
    private RtpSession rtpSession;
    private boolean isStopped;
    private boolean isTerminated;
    
    public RtpSender(PipedInputStream encodedData, RtpSession rtpSession) {
        this.encodedData = encodedData;
        this.rtpSession = rtpSession;
        isStopped = false;
        isTerminated = false;
    }
    
    public void run() {
        RtpPacket rtpPacket = new RtpPacket();
        rtpPacket.setV(2);
        rtpPacket.setPT(0);
        rtpPacket.setSSRC(1);
        int buf_size = Capture.BUFFER_SIZE / 2;
        byte[] buffer = new byte[buf_size];
        long counter = 0;
        
        while (!isStopped) {
            
            try {
                encodedData.read(buffer, 0, buf_size);
            } catch (IOException e) {
                Logger.error("input/output error", e);
                return;
            }
            
            rtpPacket.setTS(buf_size * counter++);
            rtpPacket.setPayload(buffer, buf_size);
            
            try {
                rtpSession.sendRtpPacket(rtpPacket);
            } catch (UnknownHostException e) {
                Logger.error("unknown host", e);
                return;
            } catch (RtpException e) {
                Logger.error("RTP error", e);
                return;
            } catch (IOException e) {
                Logger.error("input/output error", e);
                return;
            }
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                Logger.error("Thread interrupted", e);
                return;
            }
        }
        isTerminated = true;
    }

    public synchronized void setStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }

    public boolean isTerminated() {
        return isTerminated;
    }

}
