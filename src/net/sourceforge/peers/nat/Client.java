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
    
    Copyright 2007, 2008, 2009 Yohann Martineau 
*/

package net.sourceforge.peers.nat;

import java.io.IOException;
import java.net.InetAddress;

import net.sourceforge.peers.Logger;

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
            Logger.error("input/output error", e);
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
            Logger.error("usage: java ... <email> <localAddress> <localPort>");
            System.exit(1);
        }
        
        new Client(args[0], args[1], Integer.parseInt(args[2]));
        
    }

}
