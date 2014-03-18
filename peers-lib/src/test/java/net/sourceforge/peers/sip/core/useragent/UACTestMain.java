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

package net.sourceforge.peers.sip.core.useragent;

import java.net.SocketException;

import net.sourceforge.peers.FileLogger;
import net.sourceforge.peers.media.AbstractSoundManager;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transport.SipRequest;

public class UACTestMain {

    public static void main(String[] args) {
        String requestUri;
        UserAgent userAgent;
        SipRequest sipRequest;
        AbstractSoundManager soundManager = new DummySoundManager();
        try {
            userAgent = new UserAgent(null, (String)null, new FileLogger(null),
                    soundManager);
            requestUri = "sip:bob@" + userAgent.getConfig()
                .getLocalInetAddress().getHostAddress() + ":6060";
            sipRequest = userAgent.invite(requestUri,
                    userAgent.getConfig()
                        .getLocalInetAddress().getHostName());
        } catch (SipUriSyntaxException e) {
            e.printStackTrace();
            return;
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }
        
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        if (sipRequest != null) {
            userAgent.terminate(sipRequest);
        }
    }
}
