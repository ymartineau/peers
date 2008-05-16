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
                    System.out.println("RECEIVED:\n" + message);
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
        System.out.println("SENT:\n" + expectedMessage);
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
    
//    @Test
//    public void testCreateClientTransport() throws SipParserException,
//            UnknownHostException, IOException {
//        final int PORT = 6060;
//        final int BUF_SIZE = 2048;
//        final String MESSAGE = "hello, world !!!";
//
//        Thread thread = new Thread(new Runnable() {
//            public void run() {
//                DatagramPacket datagramPacket = new DatagramPacket(
//                        new byte[BUF_SIZE], BUF_SIZE);
//                DatagramSocket datagramSocket = null;
//                try {
//                    datagramSocket = new DatagramSocket(PORT);
//                    datagramSocket.receive(datagramPacket);
//                    byte[] receivedBytes = datagramPacket.getData();
//                    System.out.println("sent bytes length = " + MESSAGE.length());
//                    System.out.println("received bytes length = "
//                            + datagramPacket.getLength());
//                    String received = new String(receivedBytes);
//                    System.out.println("RECEIVED:\n" + received);
//                    assert datagramPacket.getLength() == MESSAGE.length();
//                    assert MESSAGE.equals(received);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    assert false;
//                }
//            }
//        });
//        thread.start();
//        
//        SipRequest sipRequest = (SipRequest)parse(MESSAGE);
//        InetAddress inetAddress = InetAddress.getLocalHost();
//        MessageSender messageSender = transportManager.createClientTransport(
//                sipRequest, inetAddress, PORT, "UDP");
//        messageSender.sendMessage(sipRequest);
//        System.out.println("SENT:\n" + MESSAGE);
//    }
    
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
