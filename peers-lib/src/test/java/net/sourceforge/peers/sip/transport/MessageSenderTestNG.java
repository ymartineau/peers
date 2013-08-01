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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.FileLogger;
import net.sourceforge.peers.JavaConfig;
import net.sourceforge.peers.Logger;
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
    private volatile int port;

    @BeforeClass
    protected void init() throws UnknownHostException, SocketException {
        //TODO interface between transport manager and transaction manager
        DatagramSocket datagramSocket = new DatagramSocket();
        final int localPort = datagramSocket.getLocalPort();
        Config config = new JavaConfig();
        config.setSipPort(localPort);
        config.setLocalInetAddress(InetAddress.getLocalHost());
        SipServerTransportUser sipServerTransportUser =
            new SipServerTransportUser() {
            @Override public void messageReceived(SipMessage sipMessage) {}
        };
        Logger logger = new FileLogger(null);
        transportManager = new TransportManager(
                new TransactionManager(logger),
                config, logger);
        transportManager.setSipServerTransportUser(sipServerTransportUser);
        transportManager.setSipPort(new Random().nextInt(65535));
    }
    
    @Test(groups = "listen")
    public void listen() throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                DatagramPacket datagramPacket = new DatagramPacket(
                        new byte[2048], 2048);
                DatagramSocket datagramSocket;
                try {
                    datagramSocket = new DatagramSocket();
                    port = datagramSocket.getLocalPort();
                    while (message == null || "".equals(message.trim())) {
                        datagramSocket.receive(datagramPacket);
                        byte[] receivedBytes = datagramPacket.getData();
                        int nbReceivedBytes = datagramPacket.getLength();
                        byte[] trimmedBytes = new byte[nbReceivedBytes];
                        System.arraycopy(receivedBytes, 0,
                                trimmedBytes, 0, nbReceivedBytes);
                        message = new String(trimmedBytes);
                    }
                    messageReceived = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        while (port == 0) {
            Thread.sleep(50);
        }
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
                sipRequest, inetAddress, port, "UDP");
        messageSender.sendMessage(sipRequest);
        expectedMessage = sipRequest.toString();
    }
    
    @Test(timeOut = 10000, dependsOnGroups = {"send"})
    public void checkAnswer() throws InterruptedException {
        while (!messageReceived) {
            Thread.sleep(50);
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
