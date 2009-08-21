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

package net.sourceforge.peers.nat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Iterator;

import net.sourceforge.peers.Logger;

import org.dom4j.Document;
import org.dom4j.Element;

public class PeerManager extends Thread {

    private InetAddress localAddress;
    private int localPort;
    private Document document;
    
    public PeerManager(InetAddress localAddress, int localPort) {
        this.localAddress = localAddress;
        this.localPort = localPort;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public void run() {
        DatagramSocket datagramSocket;
        try {
            datagramSocket = new DatagramSocket(localPort, localAddress);
        } catch (SocketException e) {
            Logger.error("socket error", e);
            return;
        }
//        UDPReceiver udpReceiver = new UDPReceiver(datagramSocket);
//        udpReceiver.start();
        while (true) {
            Element root = document.getRootElement();
            for (Iterator<?> i = root.elementIterator("peer"); i.hasNext(); ) {
                createConnection((Element)i.next(), datagramSocket);
            }
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                Logger.error("Thread interrupted", e);
                return;
            }
        }
    }
    
    private void createConnection(Element peer, DatagramSocket datagramSocket) {
        Element ipaddress = peer.element("ipaddress");
        Element port = peer.element("port");
        String ipAddressStr = ipaddress.getText().trim();
        int remotePort = Integer.parseInt(port.getText().trim());
        try {
            InetAddress remoteAddress = InetAddress.getByName(ipAddressStr);
            // DatagramSocket datagramSocket = new DatagramSocket(localPort, localAddress);
            for (int i = 0; i < 5; ++i) {
                String message = "hello world " + System.currentTimeMillis();
                byte[] buf = message.getBytes();
                DatagramPacket datagramPacket =
                    new DatagramPacket(buf, buf.length, remoteAddress, remotePort);
                datagramSocket.send(datagramPacket);
                Logger.debug("> sent:\n" + message);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Logger.error("Thread interrupted", e);
                    return;
                }
            }

            //datagramSocket.close();
        } catch (IOException e) {
            Logger.error("input/output error", e);
            return;
        }
    }
}
