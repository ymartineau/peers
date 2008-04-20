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

package net.sourceforge.peers.sip.transport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.sourceforge.peers.sip.syntaxencoding.SipParser;
import net.sourceforge.peers.sip.syntaxencoding.SipParserException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TransportManagerTestNG {

    private TransportManager transportManager;
    
    @BeforeClass
    protected void init() {
        transportManager = TransportManager.getInstance();
    }
    
    @Test
    public void testCreateClientTransport() throws SipParserException,
            UnknownHostException, IOException {
        SipRequest sipRequest = (SipRequest)parse(
                "INVITE sip:UAB@example.com SIP/2.0\r\n"
                + "Via: ;branchId=3456UGD\r\n"
                + "Subject: I know you're there,\r\n"
                + "         pick up the phone\r\n"
                + "         and talk to me!\r\n"
                + "\r\n");
        InetAddress inetAddress = InetAddress.getByName("192.168.2.1");
        MessageSender messageSender = transportManager.createClientTransport(
                sipRequest, inetAddress, 5060, "UDP");
        messageSender.sendMessage(sipRequest);
    }

    @Test (expectedExceptions = SipParserException.class)
    public void shouldThrowIfBadMessage() throws SipParserException, IOException {
        // two characters for sip line is forbidden, minimum is 3:
        // A:1
        parse("IN\r\n");
    }
    
    @Test (expectedExceptions = SipParserException.class)
    public void shouldThrowIfNoEmptyLine() throws SipParserException, IOException {
        // two characters for sip line is forbidden, minimum is 3:
        // A:1
        parse("INVITE sip:UAB@example.com SIP/2.0\r\n"
                + "Via: ;branchId=3456UGD\r\n"
                + "Subject: I know you're there,\r\n"
                + "         pick up the phone\r\n"
                + "         and talk to me!\r\n");
    }
    
    private SipMessage parse(String message) throws IOException, SipParserException {
        ByteArrayInputStream bais = new ByteArrayInputStream(message.getBytes());
        SipParser sipParser = new SipParser();
        SipMessage sipMessage = sipParser.parse(bais);
        return sipMessage;
    }
}
