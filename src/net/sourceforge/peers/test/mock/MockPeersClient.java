package net.sourceforge.peers.test.mock;

import net.sourceforge.peers.api.DataReceiver;
import net.sourceforge.peers.api.PeersClient;
import net.sourceforge.peers.api.TCPTransport;
import net.sourceforge.peers.api.UDPTransport;

public class MockPeersClient extends PeersClient {

    public MockPeersClient(String myId, DataReceiver dataReceiver) {
        super(myId, dataReceiver);
        // TODO Auto-generated constructor stub
    }

    @Override
    public TCPTransport createTCPTransport(String peerId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UDPTransport createUDPTransport(String peerId) {
        // TODO Auto-generated method stub
        return null;
    }

}
