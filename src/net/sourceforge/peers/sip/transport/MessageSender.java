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

package net.sourceforge.peers.sip.transport;

import java.io.IOException;
import java.net.InetAddress;


public abstract class MessageSender {

    protected InetAddress inetAddress;
    protected int port;
    protected int localPort;
    protected String contact;
    
    public MessageSender(InetAddress inetAddress, int port) {
        super();
        this.inetAddress = inetAddress;
        this.port = port;
    }
    
    public abstract void sendMessage(SipMessage sipMessage) throws IOException;

    public String getContact() {
        return contact;
    }

    public int getLocalPort() {
        return localPort;
    }
    
    
}
