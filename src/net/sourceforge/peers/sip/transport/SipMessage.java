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

import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;

public abstract class SipMessage {
    
    protected String sipVersion;
    protected SipHeaders sipHeaders;
    protected byte[] body;

    public SipMessage() {
        sipVersion = RFC3261.DEFAULT_SIP_VERSION;
        sipHeaders = new SipHeaders();
    }
    
    public String getSipVersion() {
        return sipVersion;
    }

    public void setSipHeaders(SipHeaders sipHeaders) {
        this.sipHeaders = sipHeaders;
    }

    public SipHeaders getSipHeaders() {
        return sipHeaders;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        SipHeaderFieldName contentLengthName =
            new SipHeaderFieldName(RFC3261.HDR_CONTENT_LENGTH);
        SipHeaderFieldValue contentLengthValue =
            sipHeaders.get(contentLengthName);
        if (contentLengthValue == null) {
            contentLengthValue = new SipHeaderFieldValue(
                    String.valueOf(body.length));
            sipHeaders.add(contentLengthName, contentLengthValue);
        } else {
            contentLengthValue.setValue(String.valueOf(body.length));
        }
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(sipHeaders.toString());
        buf.append(RFC3261.CRLF);
        if (body != null) {
            buf.append(new String(body));
        }
        return buf.toString();
    }
    
}
