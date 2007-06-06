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

import java.net.InetAddress;

public class SipTransportConnection {

    private InetAddress remoteInetAddress;

    private int remotePort;

    private String remoteTransport;// TCP or SCTP

    public SipTransportConnection(InetAddress remoteInetAddress,
            int remotePort, String remoteTransport) {
        super();
        this.remoteInetAddress = remoteInetAddress;
        this.remotePort = remotePort;
        this.remoteTransport = remoteTransport;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != SipTransportConnection.class) {
            return false;
        }
        SipTransportConnection other = (SipTransportConnection)obj;
        return remoteInetAddress.equals(other.remoteInetAddress) &&
                remotePort == other.remotePort &&
                remoteTransport.equals(other.remoteTransport);
    }
    
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(remoteInetAddress.getHostAddress());
        buf.append(':').append(remotePort).append('/').append(remoteTransport);
        return buf.toString();
    }
    
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public InetAddress getRemoteInetAddress() {
        return remoteInetAddress;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public String getRemoteTransport() {
        return remoteTransport;
    }
    
    
}
