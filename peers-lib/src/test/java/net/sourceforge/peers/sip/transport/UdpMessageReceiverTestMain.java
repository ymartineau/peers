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
import java.net.InetAddress;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.FileLogger;
import net.sourceforge.peers.JavaConfig;
import net.sourceforge.peers.sip.RFC3261;


public class UdpMessageReceiverTestMain implements Runnable {

    public void run() {
        try {
            Config config = new JavaConfig();
            config.setLocalInetAddress(InetAddress.getLocalHost());
            TransportManager transportManager = new TransportManager(null,
                    config, new FileLogger(null));
            transportManager.createServerTransport("UDP", RFC3261.TRANSPORT_DEFAULT_PORT);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
    
    public static void main(String[] args) {
        for (int i = 0; i < 5; ++i) {
            new Thread(new UdpMessageReceiverTestMain()).start();
        }
    }
}
