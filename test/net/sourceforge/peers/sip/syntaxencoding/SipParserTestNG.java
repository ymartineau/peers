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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import net.sourceforge.peers.sip.RFC3261;
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
        assert !(value instanceof SipHeaderFieldMultiValue);
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
    public void testCompactHeaders() throws SipParserException, IOException {
        SipMessage sipMessage = parse("OPTIONS sip:h@google.com SIP/2.0\r\n" +
                "i: mldkjf43532@host.domain\r\n" +
                "m: <192.168.5.6:43673>\r\n" +
                "e:gzip\r\n" +
                "l: \r\n" +
                "  15\r\n" +
                "c: text/html\r\n" +
                "f:\"Jessy James\" <sip:jenny@jones.com>;tag=kpo34fz\r\n" +
                "s:what about the wheather today\r\n" +
                "k: INVITE,BYE, CANCEL\r\n" +
                "t: john the ripper <sip:john@ripper.com;killer>;tag=kpsd4fz\r\n" +
                "v: SIP/2.0/UDP 192.168.20.2;branch=mdsif12f\r\n" +
                "v: SIP/2.0/UDP 172.20.2.168;branch=msdf343f\r\n" +
                "v: SIP/2.0/UDP 10.1.5.7;branch=mfdf343f\r\n" +
                "v: SIP/2.0/UDP 64.32.165.46;branch=m134343f\r\n" +
                "\r\n" +
                "kd\r\n" +
                "pe0_\n" +
                ";_{\r" +
                " p");
        SipHeaders sipHeaders = sipMessage.getSipHeaders();
        SipHeaderFieldValue callId =
            sipHeaders.get(new SipHeaderFieldName(RFC3261.HDR_CALLID));
        assert callId != null;
        assert callId.getValue().indexOf("ldkjf43532@host.domain") > -1;
        SipHeaderFieldValue contact =
            sipHeaders.get(new SipHeaderFieldName(RFC3261.HDR_CONTACT));
        assert contact != null;
        assert contact.getValue().indexOf("<192.168.5.6:43673>") > -1;
        SipHeaderFieldValue contentEncoding =
            sipHeaders.get(new SipHeaderFieldName(RFC3261.HDR_CONTENT_ENCODING));
        assert contentEncoding != null;
        assert contentEncoding.getValue().indexOf("gzip") > -1;
        SipHeaderFieldValue contentlLength =
            sipHeaders.get(new SipHeaderFieldName(RFC3261.HDR_CONTENT_LENGTH));
        assert contentlLength != null;
        assert contentlLength.getValue().indexOf("15") > -1;
        assert sipMessage.getBody().length == 15;
        SipHeaderFieldValue contentType =
            sipHeaders.get(new SipHeaderFieldName(RFC3261.HDR_CONTENT_TYPE));
        assert contentType != null;
        assert contentType.getValue().indexOf("text/html") > -1;
        SipHeaderFieldValue from =
            sipHeaders.get(new SipHeaderFieldName(RFC3261.HDR_FROM));
        assert from != null;
        assert from.getValue().indexOf("\"Jessy James\" <sip:jenny@jones.com>") > -1;
        SipHeaderFieldValue subject =
            sipHeaders.get(new SipHeaderFieldName(RFC3261.HDR_SUBJECT));
        assert subject != null;
        assert subject.getValue().indexOf("what about the wheather today") > -1;
        SipHeaderFieldValue supported =
            sipHeaders.get(new SipHeaderFieldName(RFC3261.HDR_SUPPORTED));
        assert supported != null;
        assert supported.getValue().indexOf("INVITE,BYE, CANCEL") > -1;
        SipHeaderFieldValue to =
            sipHeaders.get(new SipHeaderFieldName(RFC3261.HDR_TO));
        assert to != null;
        assert to.getValue().indexOf("john the ripper <sip:john@ripper.com;killer>") > -1;
        SipHeaderFieldMultiValue via = (SipHeaderFieldMultiValue)
            sipHeaders.get(new SipHeaderFieldName(RFC3261.HDR_VIA));
        assert via != null;
        List<SipHeaderFieldValue> values = via.getValues();
        assert values.get(0).getValue().indexOf("SIP/2.0/UDP 192.168.20.2") > -1;
        assert values.get(1).getValue().indexOf("SIP/2.0/UDP 172.20.2.168") > -1;
        assert values.get(2).getValue().indexOf("SIP/2.0/UDP 10.1.5.7") > -1;
        assert values.get(3).getValue().indexOf("SIP/2.0/UDP 64.32.165.46") > -1;
    }
    
    @Test
    public void testMultiValueHeader() throws IOException, SipParserException {
        SipMessage sipMessage = parse("SIP/2.0 200 OK\r\n"
                + "From: <sip:ymartineau@ippi.fr>;tag=MMnvBY6R\r\n"
                + "Call-ID: 2Lb6Uyyx-1290462442914@192.168.1.23\r\n"
                + "CSeq: 4 INVITE\r\n"
                + "Via: SIP/2.0/UDP 192.168.1.23:6060;rport=6060;branch=z9hG4bKnksFOe9ut;received=77.193.94.155\r\n"
                + "To: <sip:2233400543@sip2sip.info>;tag=rZPsRkLoKg\r\n"
                + "Content-Length: 234\r\n"
                + "Content-Type: application/sdp\r\n"
                + "Record-Route: <sip:81.23.228.129;lr;ftag=MMnvBY6R;did=1a1.f7c24a35>, <sip:85.17.186.7;lr;ftag=MMnvBY6R;did=1a1.c11e9535>, <sip:213.215.45.230;lr=on>\r\n"
                + "Contact: <sip:77.193.94.155:6062;transport=UDP>\r\n"
                + "\r\n"
                + "v=0\r\n"
                + "o=user1 881398656 1035667908 IN IP4 213.215.45.245\r\n"
                + "s=-\r\n"
                + "c=IN IP4 213.215.45.245\r\n"
                + "t=0 0\r\n"
                + "m=audio 53452 RTP/AVP 0 8 101\r\n"
                + "a=rtpmap:0 PCMU/8000\r\n"
                + "a=rtpmap:8 PCMA/8000\r\n"
                + "a=rtpmap:101 telephone-event/8000\r\n"
                + "a=sendrecv\r\n"
                + "a=nortpproxy:yes\r\n");
        SipHeaders sipHeaders = sipMessage.getSipHeaders();
        SipHeaderFieldValue recordRoute = sipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_RECORD_ROUTE));
        assert recordRoute instanceof SipHeaderFieldMultiValue;
        SipHeaderFieldMultiValue recordRouteMulti = (SipHeaderFieldMultiValue)recordRoute;
        List<SipHeaderFieldValue> values = recordRouteMulti.getValues();
        assert "<sip:81.23.228.129;lr;ftag=MMnvBY6R;did=1a1.f7c24a35>".equals(
                values.get(0).getValue().trim());
        assert "<sip:85.17.186.7;lr;ftag=MMnvBY6R;did=1a1.c11e9535>".equals(
                values.get(1).getValue().trim());
        assert "<sip:213.215.45.230;lr=on>".equals(values.get(2).getValue().trim());
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
    
    @Test (expectedExceptions = SipParserException.class)
    public void shouldThrowIfBadContentLength() throws SipParserException, IOException {
        parse("INVITE sip:bob@ietf.org\r\n" +
                "Content-Length: 10" +
                "\r\n" +
                "12345");
    }
    
    private SipMessage parse(String message) throws SipParserException, IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(message.getBytes());
        SipParser sipParser = new SipParser();
        SipMessage sipMessage = sipParser.parse(bais);
        return sipMessage;
    }
}
