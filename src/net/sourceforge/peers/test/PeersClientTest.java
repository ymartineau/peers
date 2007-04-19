package net.sourceforge.peers.test;

import junit.framework.TestCase;
import net.sourceforge.peers.api.DataReceiver;
import net.sourceforge.peers.api.PeersClient;
import net.sourceforge.peers.api.TCPTransport;
import net.sourceforge.peers.api.UDPTransport;
import net.sourceforge.peers.test.mock.MockPeersClient;

public class PeersClientTest extends TestCase {

    private PeersClient peersClient;
    
    protected void setUp() throws Exception {
        super.setUp();
        peersClient = new MockPeersClient("alice@atlanta.com", new DataReceiver(){
            public void dataReceived(byte[] data, String peerId) {
                System.out.println("received bytes from " + peerId +
                        ": " + new String(data));
            }
        });
    }

    public void testCreateUDPTransport() {
        UDPTransport transport = peersClient.createUDPTransport("bob@biloxi.com");
        transport.sendData("hello world".getBytes());
    }

    public void testCreateTCPTransport() {
        TCPTransport transport = peersClient.createTCPTransport("bob@biloxi.com");
        transport.sendData("hello world".getBytes());
        }

}
