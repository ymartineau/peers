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

package net.sourceforge.peers.sip.syntaxencoding;

import java.util.Hashtable;

import org.testng.annotations.Test;

public class SipURITestNG {

    //SUCCESS TESTS
    
    @Test
    public void testSipUri1() throws SipUriSyntaxException {
        SipURI sipUri = new SipURI("sip:alice@atlanta.com");
        assert "alice".equals(sipUri.getUserinfo());
        assert "atlanta.com".equals(sipUri.getHost());
    }
    
    @Test
    public void testSipUri2() throws SipUriSyntaxException {
        SipURI sipUri = new SipURI("sip:atlanta.com");
        assert "atlanta.com".equals(sipUri.getHost());
    }
    
    @Test
    public void testSipUri3() throws SipUriSyntaxException {
        SipURI sipUri = new SipURI("sip:atlanta.com;a");
        assert "atlanta.com".equals(sipUri.getHost());
        Hashtable<String, String> params = sipUri.getUriParameters();
        assert params != null;
        assert 1 == params.size();
        assert params.containsKey("a");
        assert "".equals(params.get("a"));
    }
    
    @Test
    public void testSipUri4() throws SipUriSyntaxException {
        SipURI sipUri = new SipURI("sip:alice@atlanta.com;a;br=3");
        assert "alice".equals(sipUri.getUserinfo());
        assert "atlanta.com".equals(sipUri.getHost());
        Hashtable<String, String> params = sipUri.getUriParameters();
        assert params != null;
        assert 2 == params.size();
        assert params.containsKey("a");
        assert params.containsKey("br");
        assert "".equals(params.get("a"));
        assert "3".equals(params.get("br"));
    }
    
    @Test
    public void testSipUri5() throws SipUriSyntaxException {
        SipURI sipUri = new SipURI("sip:alice@atlanta.com;br=3;a");
        assert "alice".equals(sipUri.getUserinfo());
        assert "atlanta.com".equals(sipUri.getHost());
        Hashtable<String, String> params = sipUri.getUriParameters();
        assert params != null;
        assert 2 == params.size();
        assert params.containsKey("a");
        assert params.containsKey("br");
        assert "".equals(params.get("a"));
        assert "3".equals(params.get("br"));
    }
    
    @Test
    public void testSipUri6() throws SipUriSyntaxException {
        SipURI sipUri = new SipURI("sip:atlanta.com:5060");
        assert "atlanta.com".equals(sipUri.getHost());
        assert 5060 == sipUri.getPort();
    }
    
    @Test
    public void testSipUri7() throws SipUriSyntaxException {
        SipURI sipUri = new SipURI("sip:alice@atlanta.com:5060;transport=TCP;rport;otherParam=2");
        assert "alice".equals(sipUri.getUserinfo());
        assert "atlanta.com".equals(sipUri.getHost());
        assert 5060 == sipUri.getPort();
        Hashtable<String, String> params = sipUri.getUriParameters();
        assert params != null;
        assert 3 == params.size();
        assert params.containsKey("transport");
        assert params.containsKey("rport");
        assert params.containsKey("otherParam");
        assert "TCP".equals(params.get("transport"));
        assert "".equals(params.get("rport"));
        assert "2".equals(params.get("otherParam"));
    }
    
    //FAILURE TESTS
    @Test (expectedExceptions = SipUriSyntaxException.class)
    public void shouldThrowIfBadSyntax() throws SipUriSyntaxException {
        new SipURI("mlsdj");
    }
}
