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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.media.MediaMode;
import net.sourceforge.peers.sip.PortProvider;
import net.sourceforge.peers.sip.syntaxencoding.SipParser;
import net.sourceforge.peers.sip.syntaxencoding.SipParserException;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;


public class UdpMessageSenderTestMain implements Runnable {

    public void run() {
        SipRequest sipRequest = (SipRequest)parse(
                "INVITE sip:UAB@example.com SIP/2.0\r\n"
                + "Via: ;branchId=3456UGD\r\n"
                + "Subject: I know you're there,\r\n"
                + "         pick up the phone\r\n"
                + "         and talk to me!\r\n"
                + "\r\n");
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName("192.168.2.1");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }
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
        TransportManager transportManager = new TransportManager(null, config,
                new Logger(null));
        try {
            MessageSender messageSender = transportManager
                    .createClientTransport(sipRequest, inetAddress, 5060, "UDP");
            messageSender.sendMessage(sipRequest);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
    
    public static void main(String[] args) {
        for (int i = 0; i < 5; ++i) {
            new Thread(new UdpMessageSenderTestMain()).start();
        }
    }

    private SipMessage parse(String message) {
        ByteArrayInputStream bais = new ByteArrayInputStream(message.getBytes());
        SipParser sipParser = new SipParser();
        SipMessage sipMessage = null;
        try {
            sipMessage = sipParser.parse(bais);
        } catch (SipParserException e) {
            e.printStackTrace();
            return null;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
        return sipMessage;
    }
}
