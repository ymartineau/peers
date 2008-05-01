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

package net.sourceforge.peers.nat.test;

import junit.framework.TestCase;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.nat.api.DataReceiver;
import net.sourceforge.peers.nat.api.PeersClient;
import net.sourceforge.peers.nat.api.TCPTransport;
import net.sourceforge.peers.nat.api.UDPTransport;
import net.sourceforge.peers.nat.test.mock.MockPeersClient;

public class PeersClientTest extends TestCase {

    private PeersClient peersClient;
    
    protected void setUp() throws Exception {
        super.setUp();
        peersClient = new MockPeersClient("alice@atlanta.com", new DataReceiver(){
            public void dataReceived(byte[] data, String peerId) {
                Logger.debug("received bytes from " + peerId + ": "
                                        + new String(data));
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
