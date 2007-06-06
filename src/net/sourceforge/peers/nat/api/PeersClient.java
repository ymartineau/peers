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

package net.sourceforge.peers.nat.api;

public abstract class PeersClient {
    
    /**
     * creates a new peers client 
     * @param myId the string identifier corresponding
     *        to the computer or to a person (email).
     * @param dataReceiver object that will receive incoming traffic.
     */
    public PeersClient(String myId, DataReceiver dataReceiver) {
        
    }

    /**
     * creates a UDP connection to a peer.
     * @param peerId unique peer identifier (email for example).
     * @return an object that allows to send data to the peer.
     */
    public abstract UDPTransport createUDPTransport(String peerId);
    
    /**
     * creates a TCP connection to a peer.
     * @param peerId unique peer identifier (email for example).
     * @return an object that allows to send data to the peer.
     */
    public abstract TCPTransport createTCPTransport(String peerId);
}
