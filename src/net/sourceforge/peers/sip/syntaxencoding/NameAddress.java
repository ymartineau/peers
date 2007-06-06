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

package net.sourceforge.peers.sip.syntaxencoding;

import net.sourceforge.peers.sip.RFC3261;

public class NameAddress {

    public static String nameAddressToUri(String nameAddress) {
        int leftPos = nameAddress.indexOf(RFC3261.LEFT_ANGLE_BRACKET);
        int rightPos = nameAddress.indexOf(RFC3261.RIGHT_ANGLE_BRACKET);
        return nameAddress.substring(leftPos + 1, rightPos);
    }
    
    protected String addrSpec;
    protected String displayName;

    public NameAddress(String addrSpec) {
        super();
        this.addrSpec = addrSpec;
    }

    public NameAddress(String addrSpec, String displayName) {
        super();
        this.addrSpec = addrSpec;
        this.displayName = displayName;
    }
    
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (displayName != null) {
            buf.append(displayName);
            buf.append(' ');
        }
        buf.append('<');
        buf.append(addrSpec);
        buf.append('>');
        return buf.toString();
    }

    public String getAddrSpec() {
        return addrSpec;
    }
    
}
