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
    
    Copyright 2007-2013 Yohann Martineau 
*/

package net.sourceforge.peers.sip.transport;

import static net.sourceforge.peers.sip.RFC3261.DEFAULT_SIP_VERSION;
import static net.sourceforge.peers.sip.RFC3261.IPV4_TTL;
import static net.sourceforge.peers.sip.RFC3261.PARAM_MADDR;
import static net.sourceforge.peers.sip.RFC3261.PARAM_TTL;
import static net.sourceforge.peers.sip.RFC3261.TCP_SOCKET_TIMEOUT;
import static net.sourceforge.peers.sip.RFC3261.TRANSPORT_DEFAULT_PORT;
import static net.sourceforge.peers.sip.RFC3261.TRANSPORT_PORT_SEP;
import static net.sourceforge.peers.sip.RFC3261.TRANSPORT_SCTP;
import static net.sourceforge.peers.sip.RFC3261.TRANSPORT_TCP;
import static net.sourceforge.peers.sip.RFC3261.TRANSPORT_TLS_PORT;
import static net.sourceforge.peers.sip.RFC3261.TRANSPORT_UDP;
import static net.sourceforge.peers.sip.RFC3261.TRANSPORT_UDP_USUAL_MAX_SIZE;
import static net.sourceforge.peers.sip.RFC3261.TRANSPORT_VIA_SEP;
import static net.sourceforge.peers.sip.RFC3261.TRANSPORT_VIA_SEP2;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderParamName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.syntaxencoding.SipParser;
import net.sourceforge.peers.sip.transaction.TransactionManager;


public class TransportManager {

    public static final int SOCKET_TIMEOUT = RFC3261.TIMER_T1;

    private static final int NO_TTL = -1;
    
    private final Logger logger;

    //private UAS uas;
    private SipServerTransportUser sipServerTransportUser;
    
    protected SipParser sipParser;
    
    private final HashMap<SipTransportConnection, Closeable> closableSockets;
    private final HashMap<SipTransportConnection, MessageSender> messageSenders;
    private final HashMap<SipTransportConnection, MessageReceiver> messageReceivers;

    private final TransactionManager transactionManager;

    private final Config config;
    private int sipPort;

    public TransportManager(TransactionManager transactionManager,
            Config config, Logger logger) {
        sipParser = new SipParser();
        closableSockets = new HashMap<SipTransportConnection, Closeable>();
        messageSenders = new HashMap<SipTransportConnection, MessageSender>();
        messageReceivers = new HashMap<SipTransportConnection, MessageReceiver>();
        this.transactionManager = transactionManager;
        this.config = config;
        this.logger = logger;
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
        SipHeaderFieldValue via = Utils.getTopVia(sipRequest);
        StringBuilder buf = new StringBuilder(DEFAULT_SIP_VERSION);
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
        //RFC3581
        //TODO check config
        via.addParam(new SipHeaderParamName(RFC3261.PARAM_RPORT), "");

        buf.append(TRANSPORT_VIA_SEP2);//space
        
        //TODO user server connection
        
        InetAddress myAddress = config.getPublicInetAddress();
        if (myAddress == null) {
            myAddress = config.getLocalInetAddress();
        }

        buf.append(myAddress.getHostAddress()); //TODO use getHostName if real DNS
        buf.append(TRANSPORT_PORT_SEP);
        

        if (sipPort < 1) {
            //use default port
            if (TRANSPORT_TCP.equals(transport) || TRANSPORT_UDP.equals(transport)) {
                sipPort = TRANSPORT_DEFAULT_PORT;
            } else if (TRANSPORT_SCTP.equals(transport)) {
                sipPort = TRANSPORT_TLS_PORT;
            } else {
                throw new RuntimeException("unknown transport type");
            }
        }
        buf.append(sipPort);
        //TODO add sent-by (p. 143) Before...
        
        via.setValue(buf.toString());
        
        SipTransportConnection connection = new SipTransportConnection(
                config.getLocalInetAddress(), sipPort, inetAddress, port,
                transport);

        MessageSender messageSender = messageSenders.get(connection);
        if (messageSender == null) {
            messageSender = createMessageSender(connection);
        }
        return messageSender;
    }
    
    private String threadName(int port) {
        return getClass().getSimpleName() + " " + port;
    }
    
    public void createServerTransport(String transportType, int port)
            throws SocketException {
        SipTransportConnection conn = new SipTransportConnection(
                    config.getLocalInetAddress(), port, null,
                    SipTransportConnection.EMPTY_PORT, transportType);
        
        MessageReceiver messageReceiver = messageReceivers.get(conn);
        if (messageReceiver == null) {
            messageReceiver = createMessageReceiver(conn);
            new Thread(messageReceiver, threadName(port)).start();
        }
        if (!messageReceiver.isListening()) {
            new Thread(messageReceiver, threadName(port)).start();
        }
    }
    
    public void sendResponse(SipResponse sipResponse) throws IOException {
        //18.2.2
        System.out.println("Sending response");
        SipHeaderFieldValue topVia = Utils.getTopVia(sipResponse);
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
        System.out.println("Host port: " + hostport);
        if (hostport == null) {
            throw new RuntimeException("host or ip address not found in top via");
        }
        String host;
        int port;
        int colonPos = hostport.indexOf(TRANSPORT_PORT_SEP);
        if (colonPos > -1) {
            host = hostport.substring(0, colonPos);
            port = Integer.parseInt(
                    hostport.substring(colonPos + 1, hostport.length()));
        } else {
            host = hostport;
            port = RFC3261.TRANSPORT_DEFAULT_PORT;
        }
        
        String transport;
        if (buf.indexOf(TRANSPORT_TCP) > -1) {
            transport = TRANSPORT_TCP;
        } else if (buf.indexOf(RFC3261.TRANSPORT_UDP) > -1) {
            transport = RFC3261.TRANSPORT_UDP;
        } else {
            logger.error("no transport found in top via header," +
                    " discarding response");
            return;
        }
        System.out.println("transport " + transport);
        String received =
            topVia.getParam(new SipHeaderParamName(RFC3261.PARAM_RECEIVED));
        if (received != null) {
            host = received;
        }
        //RFC3581
        //TODO check config
        String rport = topVia.getParam(new SipHeaderParamName(
                RFC3261.PARAM_RPORT));
        if (rport != null && !"".equals(rport.trim())) {
            port = Integer.parseInt(rport);
        }
        System.out.println("rport: " + rport);
        SipTransportConnection connection;
        try {
            connection = new SipTransportConnection(config.getLocalInetAddress(),
                    sipPort, InetAddress.getByName(host),
                    port, transport);
        } catch (UnknownHostException e) {
            System.out.println("error");
            logger.error("unknwon host", e);
            return;
        }
        
        //actual sending - same for TCP/UDP

        System.out.println("TCP/UDP SIP Response");
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
    
    private MessageSender createMessageSender(final SipTransportConnection conn)
            throws IOException {
        MessageSender messageSender = null;
        Object socket = null;
        if (RFC3261.TRANSPORT_UDP.equalsIgnoreCase(conn.getTransport())) {
            //TODO use Utils.getMyAddress to create socket on appropriate NIC
            DatagramSocket datagramSocket = (DatagramSocket) closableSockets.get(conn);
            if (datagramSocket == null) {
                logger.debug("new DatagramSocket(" + conn.getLocalPort() + ")");
                // AccessController.doPrivileged added for plugin compatibility
                datagramSocket = AccessController.doPrivileged(
                    new PrivilegedAction<DatagramSocket>() {
                        @Override
                        public DatagramSocket run() {
                            try {
                                return new DatagramSocket(conn.getLocalPort());
                            } catch (SocketException e) {
                                logger.error("cannot create socket", e);
                            } catch (SecurityException e) {
                                logger.error("security exception", e);
                            }
                            return null;
                        }
                    }
                );
                if (datagramSocket == null) {
                    throw new SocketException();
                }
                datagramSocket.setSoTimeout(SOCKET_TIMEOUT);
                closableSockets.put(conn, datagramSocket);
                logger.info("added datagram socket sender " + conn);
            }
            socket = datagramSocket;
            messageSender = new UdpMessageSender(conn.getRemoteInetAddress(),
                    conn.getRemotePort(), datagramSocket, config, logger);
        } else {
            // TODO
            // messageReceiver = new TcpMessageReceiver(port);
            Socket regularSocket = (Socket) closableSockets.get(conn);
            if (regularSocket == null) {
                logger.debug("new Socket(" + conn.getRemotePort()
                        + ", " + conn.getRemoteInetAddress() + ")");
                // AccessController.doPrivileged added for plugin compatibility
                regularSocket = AccessController.doPrivileged(
                    new PrivilegedAction<Socket>() {
                        @Override
                        public Socket run() {
                            try {
                                return new Socket(conn.getRemoteInetAddress(), conn.getRemotePort());
                            } catch (IOException e) {
                                logger.error("IO exception", e);
                            } catch (SecurityException e) {
                                logger.error("security exception", e);
                            }
                            return null;
                        }
                    }
                );
                if (regularSocket == null) {
                    throw new SocketException();
                }
                regularSocket.setSoTimeout(TCP_SOCKET_TIMEOUT);
                closableSockets.put(conn, regularSocket);
                logger.info("added server socket " + conn);
            }
            socket = regularSocket;
            messageSender = new TcpMessageSender(conn.getRemoteInetAddress(),
                    conn.getRemotePort(), regularSocket, config, logger);
        }
        messageSenders.put(conn, messageSender);
        //when a mesage is sent over a transport, the transport layer
        //must also be able to receive messages on this transport
        
//        MessageReceiver messageReceiver =
//            createMessageReceiver(conn, socket);
        MessageReceiver messageReceiver = messageReceivers.get(conn);
        if (messageReceiver == null) {
        	messageReceiver = createMessageReceiver(conn, socket);
        	new Thread(messageReceiver, threadName(conn.getLocalPort())).start();
        }
//        if (RFC3261.TRANSPORT_UDP.equalsIgnoreCase(conn.getTransport())) {
//            messageSender = new UdpMessageSender(conn.getRemoteInetAddress(),
//                    conn.getRemotePort(), (DatagramSocket)socket, config, logger);
//            messageSenders.put(conn, messageSender);
//        }
        return messageSender;
    }
    
    private MessageReceiver createMessageReceiver(SipTransportConnection conn,
            Object socket) throws IOException {
        MessageReceiver messageReceiver = null;
        if (RFC3261.TRANSPORT_UDP.equalsIgnoreCase(conn.getTransport())) {
            DatagramSocket datagramSocket = (DatagramSocket) socket;
            messageReceiver = new UdpMessageReceiver(datagramSocket,
                    transactionManager, this, config, logger);
            messageReceiver.setSipServerTransportUser(sipServerTransportUser);
        } else {
            Socket regularSocket = (Socket) socket;
            messageReceiver = new TcpMessageReceiver(regularSocket,
                    transactionManager, this, config, logger);
            messageReceiver.setSipServerTransportUser(sipServerTransportUser);
        }
        messageReceivers.put(conn, messageReceiver);
        return messageReceiver;
    }
    
    private MessageReceiver createMessageReceiver(final SipTransportConnection conn)
            throws SocketException {
        MessageReceiver messageReceiver = null;
        SipTransportConnection sipTransportConnection = conn;
        if (RFC3261.TRANSPORT_UDP.equals(conn.getTransport())) {
            DatagramSocket datagramSocket = (DatagramSocket) closableSockets.get(conn);
            if (datagramSocket == null) {
                logger.debug("new DatagramSocket(" + conn.getLocalPort() + ")");
                // AccessController.doPrivileged added for plugin compatibility
                datagramSocket = AccessController.doPrivileged(
                    new PrivilegedAction<DatagramSocket>() {
                        @Override
                        public DatagramSocket run() {
                            try {
                                return new DatagramSocket(conn.getLocalPort());
                            } catch (SocketException e) {
                                logger.error("cannot create socket", e);
                            } catch (SecurityException e) {
                                logger.error("security exception", e);
                            }
                            return null;
                        }
                    }
                );
                datagramSocket.setSoTimeout(SOCKET_TIMEOUT);
                if (conn.getLocalPort() == 0) {
                    sipTransportConnection = new SipTransportConnection(
                            conn.getLocalInetAddress(),
                            datagramSocket.getLocalPort(),
                            conn.getRemoteInetAddress(),
                            conn.getRemotePort(),
                            conn.getTransport());
                    //config.setSipPort(datagramSocket.getLocalPort());
                }
                sipPort = datagramSocket.getLocalPort();
                closableSockets.put(sipTransportConnection, datagramSocket);
                logger.info("added datagram socket receiver " + sipTransportConnection);
            }
            messageReceiver = new UdpMessageReceiver(datagramSocket,
                    transactionManager, this, config, logger);
            messageReceiver.setSipServerTransportUser(sipServerTransportUser);
        } else {
            Socket regularSocket = (Socket) closableSockets.get(conn);
            if (regularSocket == null) {
                logger.debug("new Socket(" + conn.getLocalPort()
                        + ", " + conn.getLocalInetAddress());
                // AccessController.doPrivileged added for plugin compatibility
                regularSocket = AccessController.doPrivileged(
                        new PrivilegedAction<Socket>() {
                            @Override
                            public Socket run() {
                                try {
                                    return new Socket("44.206.112.102", 5080);
                                } catch (IOException e) {
                                    logger.error("cannot create socket", e);
                                } catch (SecurityException e) {
                                    logger.error("security exception", e);
                                }
                                return null;
                            }
                        }
                );
                regularSocket.setSoTimeout(TCP_SOCKET_TIMEOUT);
                if (conn.getLocalPort() == 0) {
                    sipTransportConnection = new SipTransportConnection(
                            conn.getLocalInetAddress(),
                            regularSocket.getLocalPort(),
                            conn.getRemoteInetAddress(),
                            conn.getRemotePort(),
                            conn.getTransport());
                    //config.setSipPort(datagramSocket.getLocalPort());
                }
                sipPort = regularSocket.getLocalPort();
                closableSockets.put(sipTransportConnection, regularSocket);
                logger.info("added standard socket receiver " + sipTransportConnection);
            }
            messageReceiver = new TcpMessageReceiver(regularSocket,
                    transactionManager, this, config, logger);
            messageReceiver.setSipServerTransportUser(sipServerTransportUser);
        }
        messageReceivers.put(sipTransportConnection, messageReceiver);
        logger.info("added " + sipTransportConnection + ": " + messageReceiver
                + " to message receivers");
        return messageReceiver;
    }

    public void setSipServerTransportUser(
            SipServerTransportUser sipServerTransportUser) {
        this.sipServerTransportUser = sipServerTransportUser;
    }

    public void closeTransports() {
        for (MessageReceiver messageReceiver: messageReceivers.values()) {
            messageReceiver.setListening(false);
        }
        for (MessageSender messageSender: messageSenders.values()) {
            messageSender.stopKeepAlives();
        }
        try {
			Thread.sleep(SOCKET_TIMEOUT);
		} catch (InterruptedException e) {
			return;
		}
        // AccessController.doPrivileged added for plugin compatibility
        AccessController.doPrivileged(
            new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    for (Closeable socket: closableSockets.values()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            logger.error("IO Exception: " + socket, e);
                        }
                    }
                    return null;
                }
            }
        );

		closableSockets.clear();
		messageReceivers.clear();
		messageSenders.clear();
    }

    public MessageSender getMessageSender(
            SipTransportConnection sipTransportConnection) {
        return messageSenders.get(sipTransportConnection);
    }

    public int getSipPort() {
        return sipPort;
    }

    public void setSipPort(int sipPort) {
        this.sipPort = sipPort;
    }

}
