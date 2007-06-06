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

package net.sourceforge.peers.nat;

import java.io.IOException;
import java.net.InetAddress;

import org.dom4j.Document;

public class Client {

    private Server server;
    //private String email;
    private PeerManager peerManager;
    
    public Client(String email, String localInetAddress, int localPort) {
        //this.email = email;
        // TODO automatic global access interface discovery
        try {
            InetAddress localAddress = InetAddress.getByName(localInetAddress);
            server = new Server(localAddress, localPort);
            peerManager = new PeerManager(localAddress, localPort);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        server.update(email);
        Document document = server.getPeers(email);
        peerManager.setDocument(document);
        peerManager.start();
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        
        if (args.length != 3) {
            System.err.println("usage: java ... <email> <localAddress> <localPort>");
            System.exit(1);
        }
        
        new Client(args[0], args[1], Integer.parseInt(args[2]));
        
    }

}
