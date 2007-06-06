package net.sourceforge.peers.api;

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
