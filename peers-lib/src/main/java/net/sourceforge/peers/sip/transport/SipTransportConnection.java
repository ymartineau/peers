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
    
    Copyright 2007-2013 Yohann Martineau 
*/

package net.sourceforge.peers.sip.transport;

import java.net.InetAddress;

public class SipTransportConnection {

    public static final int EMPTY_PORT = -1;

    private InetAddress localInetAddress;
    private int localPort = EMPTY_PORT;

    private InetAddress remoteInetAddress;
    private int remotePort = EMPTY_PORT;

    private String transport;// UDP, TCP or SCTP

    public SipTransportConnection(InetAddress localInetAddress,
            int localPort, InetAddress remoteInetAddress, int remotePort,
            String transport) {
        this.localInetAddress = localInetAddress;
        this.localPort = localPort;
        this.remoteInetAddress = remoteInetAddress;
        this.remotePort = remotePort;
        this.transport = transport;
    }

  
    
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        appendInetAddress(buf, localInetAddress);
        buf.append(':');
        appendPort(buf, localPort);
        buf.append('/');
        appendInetAddress(buf, remoteInetAddress);
        buf.append(':');
        appendPort(buf, remotePort);
        buf.append('/');
        buf.append(transport.toUpperCase());
        return buf.toString();
    }

    private void appendInetAddress(StringBuffer buf, InetAddress inetAddress) {
        if (inetAddress != null) {
            buf.append(inetAddress.getHostAddress());
        } else {
            buf.append("-");
        }
    }

    private void appendPort(StringBuffer buf, int port) {
        if (port != EMPTY_PORT) {
            buf.append(port);
        } else {
            buf.append("-");
        }
    }
 

    public InetAddress getLocalInetAddress() {
        return localInetAddress;
    }

    public int getLocalPort() {
        return localPort;
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((localInetAddress == null) ? 0 : localInetAddress.hashCode());
		result = prime * result + localPort;
		result = prime * result + ((remoteInetAddress == null) ? 0 : remoteInetAddress.hashCode());
		result = prime * result + remotePort;
		result = prime * result + ((transport == null) ? 0 : transport.hashCode());
		return result;
	}

 
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SipTransportConnection other = (SipTransportConnection) obj;
		if (localInetAddress == null) {
			if (other.localInetAddress != null)
				return false;
		} else if (!localInetAddress.equals(other.localInetAddress))
			return false;
		if (localPort != other.localPort)
			return false;
		if (remoteInetAddress == null) {
			if (other.remoteInetAddress != null)
				return false;
		} else if (!remoteInetAddress.equals(other.remoteInetAddress))
			return false;
		if (remotePort != other.remotePort)
			return false;
		if (transport == null) {
			if (other.transport != null)
				return false;
		} else if (!transport.equals(other.transport))
			return false;
		return true;
	}



	public InetAddress getRemoteInetAddress() {
        return remoteInetAddress;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public String getTransport() {
        return transport;
    }
    
    
}
