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

import net.sourceforge.peers.Config;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.RFC3261;

import java.io.IOException;
import java.net.Socket;
import java.net.InetAddress;
import java.security.AccessController;
import java.security.PrivilegedAction;


public class TcpMessageSender extends MessageSender {

    private final Socket socket;

    public TcpMessageSender(InetAddress inetAddress, int port,
                            Socket socket, Config config,
                            Logger logger) {
        super(socket.getLocalPort(), inetAddress, port,
                config, RFC3261.TRANSPORT_TCP, logger);
        this.socket = socket;
    }

    @Override
    public synchronized void sendMessage(SipMessage sipMessage) throws IOException {
        logger.debug("TcpMessageSender.sendMessage");
        if (sipMessage == null) {
            return;
        }
        byte[] buf = sipMessage.toString().getBytes();
        sendBytes(buf);
        String direction = "SENT to " + inetAddress.getHostAddress() +
                "/" + port;
        logger.traceNetwork(new String(buf), direction);
    }

    @Override
    public synchronized void sendBytes(final byte[] bytes) {
        logger.debug("TcpMessageSender.sendBytes " + bytes.length
                + " " + inetAddress + ":" + port);
        // AccessController.doPrivileged added for plugin compatibility
        AccessController.doPrivileged(
            new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    try {
                        logger.debug(socket.getLocalAddress().toString());
                        socket.getOutputStream().write(bytes);
                    } catch (Exception e) {
                        logger.error("Exception", e);
                    }
                    return null;
                }
            }
        );

        logger.debug("TcpMessageSender.sendBytes packet sent");
    }

}
