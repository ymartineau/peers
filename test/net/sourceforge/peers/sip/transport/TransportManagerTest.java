/*
    This file is part of Peers.

    Peers is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Peers is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    
    Copyright 2007 Yohann Martineau 
*/

package net.sourceforge.peers.sip.transport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.sourceforge.peers.sip.syntaxencoding.SipParser;
import net.sourceforge.peers.sip.syntaxencoding.SipParserException;
import net.sourceforge.peers.sip.transport.MessageSender;
import net.sourceforge.peers.sip.transport.SipMessage;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.TransportManager;

import junit.framework.TestCase;

public class TransportManagerTest extends TestCase {

    private TransportManager transportManager;
    
    @Override
    protected void setUp() throws Exception {
        transportManager = TransportManager.getInstance();
    }
    
    public void testCreateClientTransport() {
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
            fail();
            return;
        }
        try {
            MessageSender messageSender = transportManager.createClientTransport(
                    sipRequest, inetAddress, 5060, "UDP");
            messageSender.sendMessage(sipRequest);
        } catch (IOException e) {
            fail();
            return;
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
            fail(e.getMessage());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail(ioe.getMessage());
        }
        return sipMessage;
    }
}
