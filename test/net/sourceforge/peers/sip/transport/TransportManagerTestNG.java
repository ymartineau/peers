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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.syntaxencoding.SipParser;
import net.sourceforge.peers.sip.syntaxencoding.SipParserException;
import net.sourceforge.peers.sip.transaction.TransactionManager;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TransportManagerTestNG {

    private TransportManager transportManager;
    
    @BeforeClass
    protected void init() throws UnknownHostException {
        //TODO interface between transport manager and transaction manager
        transportManager = new TransportManager(new TransactionManager(),
                InetAddress.getLocalHost(), RFC3261.TRANSPORT_DEFAULT_PORT);
    }
    
    @Test
    public void testCreateClientTransport()
            throws IOException, SipParserException {
        String testMessage = "MESSAGE sip:bob@bilox.com SIP/2.0\r\n" +
        "Via: \r\n" +
        "\r\n";
        int port = 6061;
        InetAddress localHost = InetAddress.getLocalHost();
        SipRequest sipRequest = (SipRequest)parse(testMessage);
        MessageSender messageSender = transportManager.createClientTransport(
                sipRequest, localHost, port, "UDP");
        assert messageSender != null;
        assert messageSender.getLocalPort() > 1024;
        String contact = messageSender.getContact();
        assert contact != null;
        assert !"".equals(contact.trim());
        assert contact.indexOf(localHost.getHostAddress()) > -1;
    }
    
    //TODO test createClientTransport with ttl
    
    //TODO test sendResponse
    
    @Test (expectedExceptions = SocketException.class)
    public void checkServerConnection() throws SocketException {
        try {
            transportManager.createServerTransport("UDP", 5060);
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }
        new DatagramSocket(5060);
    }

    private SipMessage parse(String message) throws IOException, SipParserException {
        ByteArrayInputStream bais = new ByteArrayInputStream(message.getBytes());
        SipParser sipParser = new SipParser();
        SipMessage sipMessage = sipParser.parse(bais);
        return sipMessage;
    }
}
