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
    
    Copyright 2007, 2008, 2009 Yohann Martineau 
*/

package net.sourceforge.peers.sip.transaction;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.sourceforge.peers.sip.PortProvider;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.syntaxencoding.SipParser;
import net.sourceforge.peers.sip.syntaxencoding.SipParserException;
import net.sourceforge.peers.sip.transport.SipMessage;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;
import net.sourceforge.peers.sip.transport.TransportManager;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TransactionManagerTestNG {

    private TransactionManager transactionManager;
    
    @BeforeClass
    public void init() throws UnknownHostException {
        transactionManager = new TransactionManager();
        TransportManager transportManager = new TransportManager(
                transactionManager,
                InetAddress.getLocalHost(),
                PortProvider.getNextPort());
        transactionManager.setTransportManager(transportManager);
    }
    
    @Test
    public void testCreateNonInviteClientTransaction()
            throws IOException, SipParserException {
        String testMessage = "MESSAGE sip:bob@biloxi.com SIP/2.0\r\n" +
        "Via: \r\n" +
        "\r\n";
        int port = PortProvider.getNextPort();
        InetAddress localHost = InetAddress.getLocalHost();
        SipRequest sipRequest = (SipRequest)parse(testMessage);
        ClientTransactionUser clientTransactionUser = new ClientTransactionUser() {
            public void errResponseReceived(SipResponse sipResponse) {
            }
            public void provResponseReceived(SipResponse sipResponse, Transaction transaction) {
            }
            public void successResponseReceived(SipResponse sipResponse, Transaction transaction) {
            }
            public void transactionTimeout() {
            }
            public void transactionTransportError() {
            }
        };
        ClientTransaction clientTransaction =
            transactionManager.createClientTransaction(sipRequest,
                    localHost,
                    port,
                    "UDP",
                    RFC3261.BRANCHID_MAGIC_COOKIE + "dsj2J347hsd23SD",
                    clientTransactionUser);
        assert clientTransaction != null;
        assert clientTransaction instanceof NonInviteClientTransaction;
        String contact = clientTransaction.getContact();
        assert contact.indexOf(localHost.getHostAddress()) > -1;
    }
    
    @Test
    public void testCreateNonInviteServerTransaction()
            throws IOException, SipParserException {
        String message = "MESSAGE sip:john@doe.co.uk SIP/2.0\r\n" +
                "Via: \r\n" +
                "\r\n";
        SipRequest sipRequest = (SipRequest)parse(message);
        String response = "SIP/2.0 200 OK\r\n" +
                "Via: \r\n" +
                "CSeq: 1\r\n" +
                "\r\n";
        SipResponse sipResponse = (SipResponse)parse(response);
        ServerTransactionUser serverTransactionUser =
            new ServerTransactionUser() {
                public void transactionFailure() {
                }
            };
        ServerTransaction serverTransaction =
            transactionManager.createServerTransaction(sipResponse,
                PortProvider.getNextPort(), "UDP", serverTransactionUser,
                sipRequest);
        assert serverTransaction != null;
        assert serverTransaction instanceof NonInviteServerTransaction;
    }
    
    @Test
    public void testInviteClientTransaction()
            throws IOException, SipParserException {
        String testMessage = "INVITE sip:bob@biloxi.com SIP/2.0\r\n" +
        "Via: \r\n" +
        "\r\n";
        int port = PortProvider.getNextPort();
        InetAddress localHost = InetAddress.getLocalHost();
        SipRequest sipRequest = (SipRequest)parse(testMessage);
        ClientTransactionUser clientTransactionUser = new ClientTransactionUser() {
            public void errResponseReceived(SipResponse sipResponse) {
            }
            public void provResponseReceived(SipResponse sipResponse, Transaction transaction) {
            }
            public void successResponseReceived(SipResponse sipResponse, Transaction transaction) {
            }
            public void transactionTimeout() {
            }
            public void transactionTransportError() {
            }
        };
        ClientTransaction clientTransaction =
            transactionManager.createClientTransaction(sipRequest,
                    localHost,
                    port,
                    "UDP",
                    RFC3261.BRANCHID_MAGIC_COOKIE + "dsj2J347hsd23SD",
                    clientTransactionUser);
        assert clientTransaction != null;
        assert clientTransaction instanceof InviteClientTransaction;
        String contact = clientTransaction.getContact();
        assert contact.indexOf(localHost.getHostAddress()) > -1;
    }
    
    @Test
    public void testCreateInviteServerTransaction()
            throws IOException, SipParserException {
        String message = "INVITE sip:john@doe.co.uk SIP/2.0\r\n" +
                "Via: \r\n" +
                "\r\n";
        SipRequest sipRequest = (SipRequest)parse(message);
        String response = "SIP/2.0 200 OK\r\n" +
                "Via: \r\n" +
                "CSeq: 1 INVITE\r\n" +
                "\r\n";
        SipResponse sipResponse = (SipResponse)parse(response);
        ServerTransactionUser serverTransactionUser =
            new ServerTransactionUser() {
                public void transactionFailure() {
                }
            };
        ServerTransaction serverTransaction =
            transactionManager.createServerTransaction(sipResponse,
                PortProvider.getNextPort(), "UDP", serverTransactionUser,
                sipRequest);
        assert serverTransaction != null;
        assert serverTransaction instanceof InviteServerTransaction;
    }
    
    private SipMessage parse(String message) throws IOException, SipParserException {
        ByteArrayInputStream bais = new ByteArrayInputStream(message.getBytes());
        SipParser sipParser = new SipParser();
        SipMessage sipMessage = sipParser.parse(bais);
        return sipMessage;
    }
}
