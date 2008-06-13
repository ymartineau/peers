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
    
    Copyright 2008 Yohann Martineau 
*/

package net.sourceforge.peers.media;

import gov.nist.jrtp.RtpException;
import gov.nist.jrtp.RtpPacket;
import gov.nist.jrtp.RtpSession;

import java.io.IOException;
import java.io.PipedInputStream;
import java.net.UnknownHostException;

public class RtpSender implements Runnable {

    private PipedInputStream encodedData;
    private RtpSession rtpSession;
    private boolean isStopped;
    
    public RtpSender(PipedInputStream encodedData, RtpSession rtpSession) {
        this.encodedData = encodedData;
        this.rtpSession = rtpSession;
    }
    
    public void run() {
        RtpPacket rtpPacket = new RtpPacket();
        rtpPacket.setV(2);
        rtpPacket.setPT(0);
        rtpPacket.setSSRC(1);
        int buf_size = 160;
        byte[] buffer = new byte[buf_size];
        long counter = 0;
        
        while (!isStopped) {
            
            try {
                encodedData.read(buffer, 0, buf_size);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            
            rtpPacket.setTS(buf_size * counter++);
            rtpPacket.setPayload(buffer, buf_size);
            
            try {
                rtpSession.sendRtpPacket(rtpPacket);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return;
            } catch (RtpException e) {
                e.printStackTrace();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public synchronized void setStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }
    
}
