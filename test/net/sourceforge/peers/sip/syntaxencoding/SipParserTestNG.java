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

import net.sourceforge.peers.sip.transport.SipMessage;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.testng.annotations.Test;

public class SipParserTestNG {

    @Test
    public void testParse() throws SipParserException, IOException {
        //request
        SipMessage sipMessage = parse("INVITE sip:UAB@example.com SIP/2.0\r\n"
                + "\r\n");
        assert SipRequest.class.equals(sipMessage.getClass());
        //response
        sipMessage = parse("SIP/2.0 100 Trying\r\n"
                + "\r\n");
        assert SipResponse.class.equals(sipMessage.getClass());
        //empty first lines
        sipMessage = parse("\r\n"
                + "\r\n"
                + "\r\n"
                + "INVITE sip:UAB@example.com SIP/2.0\r\n"
                + "\r\n");
        assert SipRequest.class.equals(sipMessage.getClass());
    }

    @Test
    public void testParseHeaders() throws SipParserException, IOException {
        SipMessage sipMessage = parse("INVITE sip:UAB@example.com SIP/2.0\r\n"
                + "Via: SIP/2.0/UDP 10.20.30.40:5060\r\n"
                + "\r\n");
        assert sipMessage != null;
        SipHeaders sipHeaders = sipMessage.getSipHeaders();
        assert 1 == sipHeaders.getCount();
        SipHeaderFieldName via = new SipHeaderFieldName("Via");
        SipHeaderFieldValue value = sipHeaders.get(via);
        assert value != null;
        assert "SIP/2.0/UDP 10.20.30.40:5060".equals(value.toString());
    }
    
    @Test
    public void testParseMultilineHeader() throws SipParserException, IOException {
        SipMessage sipMessage = parse("INVITE sip:UAB@example.com SIP/2.0\r\n"
                + "Via: SIP/2.0/UDP 10.20.30.40:5060\r\n"
                + "Subject: I know you're there,\r\n"
                + "         pick up the phone\r\n"
                + "         and talk to me!\r\n"
                + "\r\n");
        assert sipMessage != null;
        SipHeaders sipHeaders = sipMessage.getSipHeaders();
        assert 2 == sipHeaders.getCount();
        SipHeaderFieldValue value = sipHeaders.get(new SipHeaderFieldName("Subject"));
        assert "I know you're there, pick up the phone and talk to me!".equals(
                value.toString());
    }
    
    @Test
    public void testParseMultiHeader() throws SipParserException, IOException {
        SipMessage sipMessage = parse("INVITE sip:UAB@example.com SIP/2.0\r\n"
                + "Route: <sip:alice@atlanta.com>\r\n"
                + "Subject: Lunch\r\n"
                + "Route: <sip:bob@biloxi.com>\r\n"
                + "Route: <sip:carol@chicago.com>\r\n"
                + "\r\n");
        SipHeaders sipHeaders = sipMessage.getSipHeaders();
        assert sipHeaders != null;
        assert 2 == sipHeaders.getCount();
        SipHeaderFieldValue subject = sipHeaders.get(new SipHeaderFieldName("Subject"));
        assert subject != null;
        assert "Lunch".equals(subject.toString());
        
        SipHeaderFieldValue route = sipHeaders.get(new SipHeaderFieldName("Route"));
        assert route != null;
        assert "<sip:alice@atlanta.com>, <sip:bob@biloxi.com>, <sip:carol@chicago.com>"
            .equals(route.toString());
    }
    
    @Test
    public void testHeaderParams() throws SipParserException, IOException {
        SipMessage sipMessage = parse("INVITE sip:UAB@example.com SIP/2.0\r\n"
                + "Via: <sip:alice@atlanta.com>;transport=TCP\r\n"
                + "\r\n");
        SipHeaderFieldValue value =
            sipMessage.getSipHeaders().get(new SipHeaderFieldName("Via"));
        assert "TCP".equals(value.getParam(new SipHeaderParamName("transport")));
        assert "TCP".equals(value.getParam(new SipHeaderParamName("Transport")));
    }
    
    @Test
    public void testParseBody() throws SipParserException, IOException {
        SipMessage sipMessage = parse("INVITE sip:UAB@example.com SIP/2.0\r\n"
                + "Via: <sip:alice@atlanta.com>;transport=TCP\r\n"
                + "Content-Length: 15\r\n"
                + "\r\n"
                + "a=134\r\n"
                + "b=test\r\n");
        assert sipMessage != null;
        assert 2 == sipMessage.getSipHeaders().getCount();
        byte[] expectedBody = "a=134\r\nb=test\r\n".getBytes();
        byte[] realBody = sipMessage.getBody();
        assert expectedBody.length == realBody.length;
        for (int i = 0; i < expectedBody.length; ++i) {
            assert expectedBody[i] == realBody[i];
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
        assert binaryBody.length == realBody.length;
        for (int i = 0; i < expectedBody.length; ++i) {
            assert binaryBody[i] == realBody[i];
        }
    }
    
    @Test (expectedExceptions = SipParserException.class)
    public void shouldThrowIfBadMessage() throws SipParserException, IOException {
        // two characters for sip line is forbidden, minimum is 3:
        // A:1
        parse("IN\r\n");
    }
    
    @Test (expectedExceptions = SipParserException.class)
    public void shouldThrowIfNoEmptyLine() throws SipParserException, IOException {
        // two characters for sip line is forbidden, minimum is 3:
        // A:1
        parse("INVITE sip:UAB@example.com SIP/2.0\r\n"
                + "Via: ;branchId=3456UGD\r\n"
                + "Subject: I know you're there,\r\n"
                + "         pick up the phone\r\n"
                + "         and talk to me!\r\n");
    }
    
    private SipMessage parse(String message) throws SipParserException, IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(message.getBytes());
        SipParser sipParser = new SipParser();
        SipMessage sipMessage = sipParser.parse(bais);
        return sipMessage;
    }
}
