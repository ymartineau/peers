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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import net.sourceforge.peers.sip.RFC3261;


public class UdpMessageSender extends MessageSender {

    private DatagramSocket datagramSocket;
    
    public UdpMessageSender(InetAddress inetAddress, int port,
            DatagramSocket datagramSocket)
            throws SocketException {
        super(inetAddress, port);

        this.datagramSocket = datagramSocket;
        StringBuffer buf = new StringBuffer();
        buf.append(datagramSocket.getLocalAddress().getHostAddress());
        buf.append(RFC3261.TRANSPORT_PORT_SEP);
        buf.append(datagramSocket.getLocalPort());
        buf.append(RFC3261.PARAM_SEPARATOR);
        buf.append(RFC3261.PARAM_TRANSPORT);
        buf.append(RFC3261.PARAM_ASSIGNMENT);
        buf.append(RFC3261.TRANSPORT_UDP);
        contact = buf.toString();
        localPort = datagramSocket.getLocalPort();
    }

    @Override
    public synchronized void sendMessage(SipMessage sipMessage) throws IOException {
        byte[] buf = sipMessage.toString().getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, inetAddress,
                port);
        //System.out.println("socket.send(packet)");
        datagramSocket.send(packet);
    }

}
