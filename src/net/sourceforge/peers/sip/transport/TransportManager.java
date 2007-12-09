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
    
    Copyright 2007 Yohann Martineau 
*/

package net.sourceforge.peers.sip.transport;

import static net.sourceforge.peers.sip.RFC3261.DEFAULT_SIP_VERSION;
import static net.sourceforge.peers.sip.RFC3261.IPV4_TTL;
import static net.sourceforge.peers.sip.RFC3261.PARAM_MADDR;
import static net.sourceforge.peers.sip.RFC3261.PARAM_TTL;
import static net.sourceforge.peers.sip.RFC3261.TRANSPORT_DEFAULT_PORT;
import static net.sourceforge.peers.sip.RFC3261.TRANSPORT_PORT_SEP;
import static net.sourceforge.peers.sip.RFC3261.TRANSPORT_SCTP;
import static net.sourceforge.peers.sip.RFC3261.TRANSPORT_TCP;
import static net.sourceforge.peers.sip.RFC3261.TRANSPORT_TLS_PORT;
import static net.sourceforge.peers.sip.RFC3261.TRANSPORT_UDP;
import static net.sourceforge.peers.sip.RFC3261.TRANSPORT_UDP_USUAL_MAX_SIZE;
import static net.sourceforge.peers.sip.RFC3261.TRANSPORT_VIA_SEP;
import static net.sourceforge.peers.sip.RFC3261.TRANSPORT_VIA_SEP2;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;

import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderParamName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.syntaxencoding.SipParser;


public class TransportManager {

    private static TransportManager INSTANCE;
    private static int NO_TTL = -1;
    
    public static TransportManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TransportManager();
        }
        return INSTANCE;
    }
    
    protected SipParser sipParser;
    
    private Hashtable<SipTransportConnection, DatagramSocket> datagramSockets;
    private Hashtable<SipTransportConnection, MessageSender> messageSenders;
    private Hashtable<SipTransportConnection, MessageReceiver> messageReceivers;
    
    private TransportManager() {
        sipParser = new SipParser();
        datagramSockets = new Hashtable<SipTransportConnection, DatagramSocket>();
        messageSenders = new Hashtable<SipTransportConnection, MessageSender>();
        messageReceivers = new Hashtable<SipTransportConnection, MessageReceiver>();
    }
    
    public MessageSender createClientTransport(SipRequest sipRequest,
            InetAddress inetAddress, int port, String transport)
                throws IOException {
        return createClientTransport(sipRequest, inetAddress, port, transport,
                NO_TTL);
    }
    
    public MessageSender createClientTransport(SipRequest sipRequest,
            InetAddress inetAddress, int port, String transport, int ttl)
                throws IOException {
        //18.1
        
        //via created by transaction layer to add branchid
        SipHeaderFieldValue via = Utils.getInstance().getTopVia(sipRequest);
        StringBuffer buf = new StringBuffer(DEFAULT_SIP_VERSION);
        buf.append(TRANSPORT_VIA_SEP);
        if (sipRequest.toString().getBytes().length > TRANSPORT_UDP_USUAL_MAX_SIZE) {
            transport = TRANSPORT_TCP;
        }
        buf.append(transport);
        if (inetAddress.isMulticastAddress()) {
            SipHeaderParamName maddrName = new SipHeaderParamName(PARAM_MADDR);
            via.addParam(maddrName, inetAddress.getHostAddress());
            if (inetAddress instanceof Inet4Address) {
                SipHeaderParamName ttlName = new SipHeaderParamName(PARAM_TTL);
                via.addParam(ttlName, IPV4_TTL);
            }
        }
        buf.append(TRANSPORT_VIA_SEP2);//space
        
        //TODO user server connection
        

        //TODO make local address and local port configurable
        InetAddress localAddress = Utils.getInstance().getMyAddress();
        int localPort = Utils.getInstance().getSipPort();
        
        
        buf.append(localAddress.getHostAddress()); //TODO use getHostName if real DNS
        buf.append(TRANSPORT_PORT_SEP);
        

        if (localPort < 1) {
            //use default port
            if (TRANSPORT_TCP.equals(transport) || TRANSPORT_UDP.equals(transport)
                    || TRANSPORT_SCTP.equals(transport)) {
                localPort = TRANSPORT_DEFAULT_PORT;
            }
            else if (TRANSPORT_SCTP.equals(transport)) {
                localPort = TRANSPORT_TLS_PORT;
            }
            else {
                throw new RuntimeException("unknown transport type");
            }
        }
        buf.append(localPort);//no, this port must be configured
        //TODO add sent-by (p. 143) Before...
        
        via.setValue(buf.toString());
        
        SipTransportConnection connection =
            new SipTransportConnection(inetAddress, port, transport);

        MessageSender messageSender = messageSenders.get(connection);
        if (messageSender == null) {
            messageSender = createMessageSender(connection);
        }
        return messageSender;
    }
    
    
    public void createServerTransport(String transportType, int port)
            throws IOException {
        SipTransportConnection conn = new SipTransportConnection(
                    Utils.getInstance().getMyAddress(), port, transportType);
        
        MessageReceiver messageReceiver = messageReceivers.get(conn);
        if (messageReceiver == null) {
            messageReceiver = createMessageReceiver(conn);
            new Thread(messageReceiver).start();
        }
        if (!messageReceiver.isListening()) {
            new Thread(messageReceiver).start();
        }
    }
    
    public void sendResponse(SipResponse sipResponse) throws IOException {
        //18.2.2
        SipHeaderFieldValue topVia = Utils.getInstance().getTopVia(sipResponse);
        String topViaValue = topVia.getValue();
        StringBuffer buf = new StringBuffer(topViaValue);
        String hostport = null;
        int i = topViaValue.length() - 1;
        while (i > 0) {
            char c = buf.charAt(i);
            if (c == ' ' || c == '\t') {
                hostport = buf.substring(i + 1);
                break;
            }
            --i;
        }
        if (hostport == null) {
            throw new RuntimeException("host or ip address not found in top via");
        }
        String host;
        int port;
        int colonPos = hostport.indexOf(RFC3261.TRANSPORT_PORT_SEP);
        if (colonPos > -1) {
            host = hostport.substring(0, colonPos);
            port = Integer.parseInt(
                    hostport.substring(colonPos + 1, hostport.length()));
        } else {
            host = hostport;
            port = RFC3261.TRANSPORT_DEFAULT_PORT;
        }
        
        String transport;
        if (buf.indexOf(RFC3261.TRANSPORT_TCP) > -1) {
            transport = RFC3261.TRANSPORT_TCP;
        } else if (buf.indexOf(RFC3261.TRANSPORT_UDP) > -1) {
            transport = RFC3261.TRANSPORT_UDP;
        } else {
            System.err.println("no transport found in top via header," +
                    " discarding response");
            return;
        }
        
        String received =
            topVia.getParam(new SipHeaderParamName(RFC3261.PARAM_RECEIVED));
        if (received != null) {
            host = received;
        }
        SipTransportConnection connection;
        try {
            connection = new SipTransportConnection(
                    InetAddress.getByName(host), port, transport);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }
        
        //actual sending
        
        //TODO manage maddr parameter in top via for multicast
        if (buf.indexOf(RFC3261.TRANSPORT_TCP) > -1) {
//            Socket socket = (Socket)factory.connections.get(connection);
//            if (!socket.isClosed()) {
//                try {
//                    socket.getOutputStream().write(data);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return;
//                    //TODO
//                }
//            } else {
//                try {
//                    socket = new Socket(host, port);
//                    factory.connections.put(connection, socket);
//                    socket.getOutputStream().write(data);
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                    /*
//                     * TODO
//                     * If connection attempt fails, use the procedures in RFC3263
//                     * for servers in order to determine the IP address and
//                     * port to open the connection and send the response to.
//                     */
//                    return;
//                }
//            }
        } else {
            MessageSender messageSender = messageSenders.get(connection);
            if (messageSender == null) {
                messageSender = createMessageSender(connection);
            }
            //add contact header
            SipHeaderFieldName contactName = new SipHeaderFieldName(RFC3261.HDR_CONTACT);
            SipHeaders respHeaders = sipResponse.getSipHeaders();
            StringBuffer contactBuf = new StringBuffer();
            contactBuf.append(RFC3261.LEFT_ANGLE_BRACKET);
            contactBuf.append(RFC3261.SIP_SCHEME);
            contactBuf.append(RFC3261.SCHEME_SEPARATOR);
            contactBuf.append(messageSender.getContact());
            contactBuf.append(RFC3261.RIGHT_ANGLE_BRACKET);
            respHeaders.add(contactName, new SipHeaderFieldValue(contactBuf.toString()));
            messageSender.sendMessage(sipResponse);

        }
        
        
    }
    
    private MessageSender createMessageSender(SipTransportConnection conn)
            throws IOException {
        MessageSender messageSender = null;
        Object socket = null;
        if (RFC3261.TRANSPORT_UDP.equalsIgnoreCase(conn.getRemoteTransport())) {
            //TODO use Utils.getMyAddress to create socket on appropriate NIC
            DatagramSocket datagramSocket = datagramSockets.get(conn);
            if (datagramSocket == null) {
                datagramSocket = new DatagramSocket();
                datagramSockets.put(conn, datagramSocket);
                System.out.println("added datagram socket " + conn);
            }
            socket = datagramSocket;
            messageSender = new UdpMessageSender(conn.getRemoteInetAddress(),
                    conn.getRemotePort(), datagramSocket);
        } else {
            // TODO
            // messageReceiver = new TcpMessageReceiver(port);
        }
        messageSenders.put(conn, messageSender);
        //when a mesage is sent over a transport, the transport layer
        //must also be able to receive messages on this transport
        SipTransportConnection serverConn = new SipTransportConnection(
                Utils.getInstance().getMyAddress(),
                messageSender.getLocalPort(),
                conn.getRemoteTransport());
        
        MessageReceiver messageReceiver =
            createMessageReceiver(serverConn, socket);
        new Thread(messageReceiver).start();
        return messageSender;
    }
    
    private MessageReceiver createMessageReceiver(SipTransportConnection conn,
            Object socket) throws IOException {
        MessageReceiver messageReceiver = null;
        if (RFC3261.TRANSPORT_UDP.equalsIgnoreCase(conn.getRemoteTransport())) {
            DatagramSocket datagramSocket = (DatagramSocket)socket;
            messageReceiver = new UdpMessageReceiver(datagramSocket);
        }
        messageReceivers.put(conn, messageReceiver);
        return messageReceiver;
    }
    
    private MessageReceiver createMessageReceiver(SipTransportConnection conn)
            throws IOException {
        MessageReceiver messageReceiver = null;
        System.out.println("adding " + conn + ": " + messageReceiver
                + " to message receivers");
        if (RFC3261.TRANSPORT_UDP.equals(conn.getRemoteTransport())) {
            DatagramSocket datagramSocket = datagramSockets.get(conn);
            if (datagramSocket == null) {
                datagramSocket = new DatagramSocket(conn.getRemotePort());
                datagramSockets.put(conn, datagramSocket);
                System.out.println("added datagram socket " + conn);
            }
            messageReceiver = new UdpMessageReceiver(datagramSocket);
            //TODO create also tcp receiver using a recursive call
        } else {
            //TODO
            //messageReceiver = new TcpMessageReceiver(port);
        }
        messageReceivers.put(conn, messageReceiver);
        return messageReceiver;
    }
    
    
}
