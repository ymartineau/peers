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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderParamName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.syntaxencoding.SipParser;
import net.sourceforge.peers.sip.syntaxencoding.SipParserException;
import net.sourceforge.peers.sip.transport.SipMessage;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import junit.framework.TestCase;

public class SipParserTest extends TestCase {

    public void testParse() {
        //request
        SipMessage sipMessage = parse("INVITE sip:UAB@example.com SIP/2.0\r\n"
                + "\r\n");
        assertEquals(SipRequest.class, sipMessage.getClass());
        //response
        sipMessage = parse("SIP/2.0 100 Trying\r\n"
                + "\r\n");
        assertEquals(SipResponse.class, sipMessage.getClass());
        //empty first lines
        sipMessage = parse("\r\n"
                + "\r\n"
                + "\r\n"
                + "INVITE sip:UAB@example.com SIP/2.0\r\n"
                + "\r\n");
        assertEquals(SipRequest.class, sipMessage.getClass());
    }

    public void testParseHeaders() {
        SipMessage sipMessage = parse("INVITE sip:UAB@example.com SIP/2.0\r\n"
                + "Via: SIP/2.0/UDP 10.20.30.40:5060\r\n"
                + "\r\n");
        assertNotNull(sipMessage);
        SipHeaders sipHeaders = sipMessage.getSipHeaders();
        assertEquals(1, sipHeaders.getCount());
        SipHeaderFieldName via = new SipHeaderFieldName("Via");
        SipHeaderFieldValue value = sipHeaders.get(via);
        assertNotNull(value);
        assertEquals("SIP/2.0/UDP 10.20.30.40:5060", value.toString());
    }
    
    public void testParseMultilineHeader() {
        SipMessage sipMessage = parse("INVITE sip:UAB@example.com SIP/2.0\r\n"
                + "Via: SIP/2.0/UDP 10.20.30.40:5060\r\n"
                + "Subject: I know you're there,\r\n"
                + "         pick up the phone\r\n"
                + "         and talk to me!\r\n"
                + "\r\n");
        assertNotNull(sipMessage);
        SipHeaders sipHeaders = sipMessage.getSipHeaders();
        assertEquals(2, sipHeaders.getCount());
        SipHeaderFieldValue value = sipHeaders.get(new SipHeaderFieldName("Subject"));
        assertEquals("I know you're there, pick up the phone and talk to me!", value.toString());
    }
    
    public void testParseMultiHeader() {
        SipMessage sipMessage = parse("INVITE sip:UAB@example.com SIP/2.0\r\n"
                + "Route: <sip:alice@atlanta.com>\r\n"
                + "Subject: Lunch\r\n"
                + "Route: <sip:bob@biloxi.com>\r\n"
                + "Route: <sip:carol@chicago.com>\r\n"
                + "\r\n");
        SipHeaders sipHeaders = sipMessage.getSipHeaders();
        assertNotNull(sipHeaders);
        assertEquals(2, sipHeaders.getCount());
        SipHeaderFieldValue subject = sipHeaders.get(new SipHeaderFieldName("Subject"));
        assertNotNull(subject);
        assertEquals("Lunch", subject.toString());
        
        SipHeaderFieldValue route = sipHeaders.get(new SipHeaderFieldName("Route"));
        assertNotNull(route);
        assertEquals("<sip:alice@atlanta.com>, <sip:bob@biloxi.com>, <sip:carol@chicago.com>",
                route.toString());
    }
    
    public void testHeaderParams() {
        SipMessage sipMessage = parse("INVITE sip:UAB@example.com SIP/2.0\r\n"
                + "Via: <sip:alice@atlanta.com>;transport=TCP\r\n"
                + "\r\n");
        SipHeaderFieldValue value =
            sipMessage.getSipHeaders().get(new SipHeaderFieldName("Via"));
        assertEquals("TCP", value.getParam(new SipHeaderParamName("transport")));
        assertEquals("TCP", value.getParam(new SipHeaderParamName("Transport")));
    }
    
    public void testParseBody() {
        SipMessage sipMessage = parse("INVITE sip:UAB@example.com SIP/2.0\r\n"
                + "Via: <sip:alice@atlanta.com>;transport=TCP\r\n"
                + "Content-Length: 15\r\n"
                + "\r\n"
                + "a=134\r\n"
                + "b=test\r\n");
        assertNotNull(sipMessage);
        assertEquals(2, sipMessage.getSipHeaders().getCount());
        byte[] expectedBody = "a=134\r\nb=test\r\n".getBytes();
        byte[] realBody = sipMessage.getBody();
        assertEquals(expectedBody.length, realBody.length);
        for (int i = 0; i < expectedBody.length; ++i) {
            assertEquals(expectedBody[i], realBody[i]);
        }
        
        byte[] binaryBody = new byte[0x10];
        for (byte i = 0x0; i < binaryBody.length; ++i) {
            binaryBody[i] = i;
        }
        sipMessage = parse("INVITE sip:UAB@example.com SIP/2.0\r\n"
                + "Via: <sip:alice@atlanta.com>;transport=TCP\r\n"
                + "Content-Length: " + binaryBody.length + "\r\n"
                + "\r\n"
                + new String(binaryBody));
        realBody = sipMessage.getBody();
        assertEquals(binaryBody.length, realBody.length);
        for (int i = 0; i < expectedBody.length; ++i) {
            assertEquals(binaryBody[i], realBody[i]);
        }
    }
    
    private SipMessage parse(String message) {
        ByteArrayInputStream bais = new ByteArrayInputStream(message.getBytes());
        SipParser sipParser = new SipParser();
        SipMessage sipMessage = null;
        try {
            sipMessage = sipParser.parse(bais);
        } catch (SipParserException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail(ioe.getMessage());
        }
        return sipMessage;
    }
}
