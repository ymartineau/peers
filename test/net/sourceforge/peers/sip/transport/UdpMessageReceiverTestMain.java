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

package net.sourceforge.peers.sip.transport;

import java.io.IOException;

import net.sourceforge.peers.sip.transport.TransportManager;


public class UdpMessageReceiverTestMain implements Runnable {

    public void run() {
        try {
            TransportManager.getInstance().createServerTransport("UDP", 5060);
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
