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

package net.sourceforge.peers.sip.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.media.MediaMode;
import net.sourceforge.peers.sip.PortProvider;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;


public class UdpMessageReceiverTestMain implements Runnable {

    public void run() {
        try {
            Config config = new Config() {
                
                @Override public void setUserPart(String userPart) {}
                @Override public void setSipPort(int sipPort) {}
                @Override public void setRtpPort(int rtpPort) {}
                @Override public void setPassword(String password) {}
                @Override public void setOutboundProxy(SipURI outboundProxy) {}
                @Override public void setMediaMode(MediaMode mediaMode) {}
                @Override public void setMediaDebug(boolean mediaDebug) {}
                @Override public void setLocalInetAddress(InetAddress inetAddress) {}
                @Override public void setPublicInetAddress(InetAddress inetAddress) {}
                @Override public void setDomain(String domain) {}
                @Override public void save() {}
                @Override public boolean isMediaDebug() {
                    return false;
                }
                @Override public String getUserPart() {
                    return null;
                }
                @Override
                public int getSipPort() {
                    return PortProvider.getNextPort();
                }
                @Override
                public int getRtpPort() {
                    return 0;
                }
                @Override
                public String getPassword() {
                    return null;
                }
                @Override
                public SipURI getOutboundProxy() {
                    return null;
                }
                @Override
                public MediaMode getMediaMode() {
                    return null;
                }
                @Override
                public InetAddress getLocalInetAddress() {
                    InetAddress inetAddress;
                    try {
                        inetAddress = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        throw new AssertionError();
                    }
                    return inetAddress;
                }
                @Override
                public InetAddress getPublicInetAddress() {
                    return null;
                }
                @Override
                public String getDomain() {
                    return null;
                }
            };
            TransportManager transportManager = new TransportManager(null,
                    config, new Logger(null));
            transportManager.createServerTransport("UDP", RFC3261.TRANSPORT_DEFAULT_PORT);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
    
    public static void main(String[] args) {
        for (int i = 0; i < 5; ++i) {
            new Thread(new UdpMessageReceiverTestMain()).start();
        }
    }
}
