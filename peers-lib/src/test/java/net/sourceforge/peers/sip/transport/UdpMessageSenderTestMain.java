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
import net.sourceforge.peers.FileLogger;
import net.sourceforge.peers.JavaConfig;
import net.sourceforge.peers.sip.syntaxencoding.SipParser;
import net.sourceforge.peers.sip.syntaxencoding.SipParserException;


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
        Config config = new JavaConfig();
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        config.setLocalInetAddress(localhost);
        TransportManager transportManager = new TransportManager(
                null, config, new FileLogger(null));
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
