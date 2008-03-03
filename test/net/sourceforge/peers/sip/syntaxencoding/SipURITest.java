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

import java.util.Hashtable;

import net.sourceforge.peers.sip.syntaxencoding.SipURI;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;

import junit.framework.TestCase;

public class SipURITest extends TestCase {

    public void testSipUri1() {
        try {
            SipURI sipUri = new SipURI("sip:alice@atlanta.com");
            assertEquals("alice", sipUri.getUserinfo());
            assertEquals("atlanta.com", sipUri.getHost().getHostName());
        } catch (SipUriSyntaxException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    public void testSipUri2() {
        try {
            SipURI sipUri = new SipURI("sip:atlanta.com");
            assertEquals("atlanta.com", sipUri.getHost().getHostName());
        } catch (SipUriSyntaxException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    public void testSipUri3() {
        try {
            SipURI sipUri = new SipURI("sip:atlanta.com;a");
            assertEquals("atlanta.com", sipUri.getHost().getHostName());
            Hashtable<String, String> params = sipUri.getUriParameters();
            assertNotNull(params);
            assertEquals(1, params.size());
            assertEquals(true, params.containsKey("a"));
            assertEquals("", params.get("a"));
        } catch (SipUriSyntaxException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    public void testSipUri4() {
        try {
            SipURI sipUri = new SipURI("sip:alice@atlanta.com;a;br=3");
            assertEquals("alice", sipUri.getUserinfo());
            assertEquals("atlanta.com", sipUri.getHost().getHostName());
            Hashtable<String, String> params = sipUri.getUriParameters();
            assertNotNull(params);
            assertEquals(2, params.size());
            assertEquals(true, params.containsKey("a"));
            assertEquals(true, params.containsKey("br"));
            assertEquals("", params.get("a"));
            assertEquals("3", params.get("br"));
        } catch (SipUriSyntaxException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    public void testSipUri5() {
        try {
            SipURI sipUri = new SipURI("sip:alice@atlanta.com;br=3;a");
            assertEquals("alice", sipUri.getUserinfo());
            assertEquals("atlanta.com", sipUri.getHost().getHostName());
            Hashtable<String, String> params = sipUri.getUriParameters();
            assertNotNull(params);
            assertEquals(2, params.size());
            assertEquals(true, params.containsKey("a"));
            assertEquals(true, params.containsKey("br"));
            assertEquals("", params.get("a"));
            assertEquals("3", params.get("br"));
        } catch (SipUriSyntaxException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    public void testSipUri6() {
        try {
            SipURI sipUri = new SipURI("sip:atlanta.com:5060");
            assertEquals("atlanta.com", sipUri.getHost().getHostName());
            assertEquals(5060, sipUri.getPort());
        } catch (SipUriSyntaxException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    public void testSipUri7() {
        try {
            SipURI sipUri = new SipURI("sip:alice@atlanta.com:5060;transport=TCP;rport;otherParam=2");
            assertEquals("alice", sipUri.getUserinfo());
            assertEquals("atlanta.com", sipUri.getHost().getHostName());
            assertEquals(5060, sipUri.getPort());
            Hashtable<String, String> params = sipUri.getUriParameters();
            assertNotNull(params);
            assertEquals(3, params.size());
            assertEquals(true, params.containsKey("transport"));
            assertEquals(true, params.containsKey("rport"));
            assertEquals(true, params.containsKey("otherParam"));
            assertEquals("TCP", params.get("transport"));
            assertEquals("", params.get("rport"));
            assertEquals("2", params.get("otherParam"));
        } catch (SipUriSyntaxException e) {
            e.printStackTrace();
            fail();
        }
    }
    
}
