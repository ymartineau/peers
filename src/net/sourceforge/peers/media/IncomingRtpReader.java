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
    
    Copyright 2007, 2008, 2009, 2010 Yohann Martineau 
*/

package net.sourceforge.peers.media;

import java.io.IOException;

import net.sourceforge.peers.rtp.RtpListener;
import net.sourceforge.peers.rtp.RtpPacket;
import net.sourceforge.peers.rtp.RtpSession;

public class IncomingRtpReader implements RtpListener {

    private RtpSession rtpSession;
    private SoundManager soundManager;

    public IncomingRtpReader(RtpSession rtpSession,
            SoundManager soundManager) throws IOException { 
        super();
        this.rtpSession = rtpSession;
        this.soundManager = soundManager;
        rtpSession.addRtpListener(this);
    }
    
    public void start() {
        rtpSession.start();
    }
    
    public synchronized void stop() {
        rtpSession.stop();
    }

    @Override
    public void receivedRtpPacket(RtpPacket rtpPacket) {
        byte[] data = rtpPacket.getData();

        byte[] rawBuf = new byte[data.length * 2];
        for (int i = 0; i < data.length; ++i) {
            short decoded = AudioUlawEncodeDecode02.decode(data[i]);
            rawBuf[2 * i] = (byte)(decoded & 0xFF);
            rawBuf[2 * i + 1] = (byte)(decoded >>> 8);
        }
        soundManager.writeData(rawBuf, 0, rawBuf.length);
    }

}
