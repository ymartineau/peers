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

package net.sourceforge.peers.sip.core.useragent.handlers;

import java.util.Observable;

import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderParamName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

public abstract class MethodHandler extends Observable {

    protected SipResponse buildGenericResponse(SipRequest sipRequest,
            int statusCode, String reasonPhrase) {
        //8.2.6
        SipResponse sipResponse = new SipResponse(statusCode, reasonPhrase);
        SipHeaders respHeaders = sipResponse.getSipHeaders();
        SipHeaders reqHeaders = sipRequest.getSipHeaders();
        SipHeaderFieldName fromName = new SipHeaderFieldName(RFC3261.HDR_FROM);
        respHeaders.add(fromName, reqHeaders.get(fromName));
        SipHeaderFieldName callIdName = new SipHeaderFieldName(RFC3261.HDR_CALLID);
        respHeaders.add(callIdName, reqHeaders.get(callIdName));
        SipHeaderFieldName cseqName = new SipHeaderFieldName(RFC3261.HDR_CSEQ);
        respHeaders.add(cseqName, reqHeaders.get(cseqName));
        SipHeaderFieldName viaName = new SipHeaderFieldName(RFC3261.HDR_VIA);
        respHeaders.add(viaName, reqHeaders.get(viaName));
        SipHeaderFieldName toName = new SipHeaderFieldName(RFC3261.HDR_TO);
        String to = reqHeaders.get(toName).getValue();
        SipHeaderFieldValue toValue = new SipHeaderFieldValue(to);
        toValue.addParam(new SipHeaderParamName(RFC3261.PARAM_TAG),
                Utils.getInstance().randomString(10));// TODO 19.3
        respHeaders.add(toName, toValue);
        return sipResponse;
    }
}
