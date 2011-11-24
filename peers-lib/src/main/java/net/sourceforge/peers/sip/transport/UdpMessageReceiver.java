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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.transaction.TransactionManager;


public class UdpMessageReceiver extends MessageReceiver {

    private DatagramSocket datagramSocket;
    
    public UdpMessageReceiver(DatagramSocket datagramSocket,
            TransactionManager transactionManager,
            TransportManager transportManager, Config config,
            Logger logger)
            throws SocketException {
        super(datagramSocket.getLocalPort(), transactionManager,
                transportManager, config, logger);
        this.datagramSocket = datagramSocket;
    }

    @Override
    protected void listen() throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            datagramSocket.receive(packet);
        } catch (SocketTimeoutException e) {
            return;
        }
        byte[] trimmedPacket = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), 0,
                trimmedPacket, 0, trimmedPacket.length);
        processMessage(trimmedPacket, packet.getAddress(),
                packet.getPort(), RFC3261.TRANSPORT_UDP);
    }


}
