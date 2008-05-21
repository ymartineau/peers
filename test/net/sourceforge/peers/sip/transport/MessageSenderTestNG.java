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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.syntaxencoding.SipParser;
import net.sourceforge.peers.sip.syntaxencoding.SipParserException;
import net.sourceforge.peers.sip.transaction.TransactionManager;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MessageSenderTestNG {
    
    private TransportManager transportManager;
    
    private String message;
    private String expectedMessage;
    private volatile boolean messageReceived = false;

    @BeforeClass
    protected void init() throws UnknownHostException {
        //TODO interface between transport manager and transaction manager
        transportManager = new TransportManager(new TransactionManager(),
                InetAddress.getLocalHost(), RFC3261.TRANSPORT_DEFAULT_PORT);
    }
    
    @Test(groups = "listen")
    public void listen() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                DatagramPacket datagramPacket = new DatagramPacket(
                        new byte[2048], 2048);
                DatagramSocket datagramSocket;
                try {
                    datagramSocket = new DatagramSocket(6060);
                    datagramSocket.receive(datagramPacket);
                    byte[] receivedBytes = datagramPacket.getData();
                    int nbReceivedBytes = datagramPacket.getLength();
                    byte[] trimmedBytes = new byte[nbReceivedBytes];
                    System.arraycopy(receivedBytes, 0,
                            trimmedBytes, 0, nbReceivedBytes);
                    message = new String(trimmedBytes);
                    messageReceived = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    
    @Test(dependsOnGroups = {"listen"}, groups = "send")
    public void sendMessage() throws IOException, SipParserException {
        //TODO make parser throw sipparserexception with explicit message if first
        //line is incorrect, or if there is no via (no nullpointer exception!!)
        String testMessage = "MESSAGE sip:bob@bilox.com SIP/2.0\r\n" +
            "Via: \r\n" +
            "\r\n";
        SipRequest sipRequest = (SipRequest)parse(testMessage);
        assert sipRequest.getBody() == null;
        InetAddress inetAddress = InetAddress.getLocalHost();
        assert transportManager != null;
        MessageSender messageSender = transportManager.createClientTransport(
                sipRequest, inetAddress, 6060, "UDP");
        messageSender.sendMessage(sipRequest);
        expectedMessage = sipRequest.toString();
    }
    
    @Test(timeOut = 10000, dependsOnGroups = {"send"})
    public void checkAnswer() throws InterruptedException {
        while (!messageReceived) {
            Thread.sleep(1000);
        }
        assert message != null : "message is null";
        assert expectedMessage != null : "expected message is null";
        assert message.length() == expectedMessage.length() : "message " +
                "differs from expected message length";
        assert message.equals(expectedMessage) : "message != expected message";
    }
    
    private SipMessage parse(String message) throws IOException, SipParserException {
        ByteArrayInputStream bais = new ByteArrayInputStream(message.getBytes());
        SipParser sipParser = new SipParser();
        SipMessage sipMessage = sipParser.parse(bais);
        return sipMessage;
    }
    
}
