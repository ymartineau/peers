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

package net.sourceforge.peers.sip.syntaxencoding;

import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import junit.framework.TestCase;

public class SipHeaderFieldValueTest extends TestCase {

    public void testSetValue() {
        SipHeaderFieldValue value = new SipHeaderFieldValue(";branch=13456787654");
        value.setValue("SIP/2.0/UDP 127.0.0.1:5060");
        assertEquals("SIP/2.0/UDP 127.0.0.1:5060;branch=13456787654", value.toString());
    }

}
