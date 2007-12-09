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
