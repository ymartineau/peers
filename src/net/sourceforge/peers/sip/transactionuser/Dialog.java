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

package net.sourceforge.peers.sip.transactionuser;

import java.util.ArrayList;

import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldMultiValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderParamName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transport.SipRequest;


public class Dialog {

    public static final char ID_SEPARATOR = '|';
    public static final int EMPTY_CSEQ = -1;
    
    public final DialogState INIT;
    public final DialogState EARLY;
    public final DialogState CONFIRMED;
    public final DialogState TERMINATED;

    private DialogState state;
    
    private String callId;
    private String localTag;
    private String remoteTag;
    
    private int localCSeq;
    private int remoteCSeq;
    private String localUri;
    private String remoteUri;
    private String remoteTarget;
    private boolean secure;
    private ArrayList<String> routeSet;
    
    Dialog(String callId, String localTag, String remoteTag) {
        super();
        this.callId = callId;
        this.localTag = localTag;
        this.remoteTag = remoteTag;
        
        INIT = new DialogStateInit(getId(), this);
        EARLY = new DialogStateEarly(getId(), this);
        CONFIRMED = new DialogStateConfirmed(getId(), this);
        TERMINATED = new DialogStateTerminated(getId(), this);
        
        state = INIT;
        
        localCSeq = EMPTY_CSEQ;
        remoteCSeq = EMPTY_CSEQ;
    }

    public void receivedOrSent1xx() {
        state.receivedOrSent101To199();
    }
    
    public void receivedOrSent2xx() {
        state.receivedOrSent2xx();
    }
    
    public void receivedOrSent300To699() {
        state.receivedOrSent300To699();
    }
    
    public void receivedOrSentBye() {
        state.receivedOrSentBye();
    }
    
    public void setState(DialogState state) {
        this.state = state;
    }
    
    public SipRequest buildSubsequentRequest(String method) {
        //12.2.1.1
        SipURI sipUri;
        try {
            sipUri = new SipURI(remoteTarget);
        } catch (SipUriSyntaxException e) {
            throw new RuntimeException(e);
            //TODO check remote target when message is received
        }
        SipRequest subsequentRequest = new SipRequest(method, sipUri);
        SipHeaders headers = subsequentRequest.getSipHeaders();
        
        //To
        
        SipHeaderFieldValue to = new SipHeaderFieldValue(remoteUri);
        if (remoteTag != null) {
            to.addParam(new SipHeaderParamName(RFC3261.PARAM_TAG), remoteTag);
        }
        headers.add(new SipHeaderFieldName(RFC3261.HDR_TO), to);
        
        //From
        
        SipHeaderFieldValue from = new SipHeaderFieldValue(localUri);
        if (localTag != null) {
            from.addParam(new SipHeaderParamName(RFC3261.PARAM_TAG), localTag);
        }
        headers.add(new SipHeaderFieldName(RFC3261.HDR_FROM), from);
        
        //Call-ID
        
        SipHeaderFieldValue callIdValue = new SipHeaderFieldValue(callId);
        headers.add(new SipHeaderFieldName(RFC3261.HDR_CALLID), callIdValue);
        
        //CSeq
        
        if (localCSeq == Dialog.EMPTY_CSEQ) {
            localCSeq = ((int)(System.currentTimeMillis() / 1000) & 0xFFFFFFFE) >> 1;
        } else {
            localCSeq++;
        }
        headers.add(new SipHeaderFieldName(RFC3261.HDR_CSEQ),
                new SipHeaderFieldValue(localCSeq + " " + method));
        
        //Route
        
        if (!routeSet.isEmpty()) {
            if (routeSet.get(0).contains(RFC3261.LOOSE_ROUTING)) {
                ArrayList<SipHeaderFieldValue> routes = new ArrayList<SipHeaderFieldValue>();
                for (String route : routeSet) {
                    routes.add(new SipHeaderFieldValue(route));
                }
                headers.add(new SipHeaderFieldName(RFC3261.HDR_ROUTE),
                        new SipHeaderFieldMultiValue(routes));
            } else {
                System.err.println("Trying to forward to a strict router, forbidden in this implementation");
            }
        }
        
        Utils.getInstance().addCommonHeaders(headers);
        
        return subsequentRequest;
    }
    
    public String getId() {
        StringBuffer buf = new StringBuffer();
        buf.append(callId).append(ID_SEPARATOR);
        buf.append(localTag).append(ID_SEPARATOR);
        buf.append(remoteTag);
        return buf.toString();
    }
    
    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }
    
    public int getLocalCSeq() {
        return localCSeq;
    }

    public void setLocalCSeq(int localCSeq) {
        this.localCSeq = localCSeq;
    }

    public String getLocalUri() {
        return localUri;
    }

    public void setLocalUri(String localUri) {
        this.localUri = localUri;
    }

    public int getRemoteCSeq() {
        return remoteCSeq;
    }

    public void setRemoteCSeq(int remoteCSeq) {
        this.remoteCSeq = remoteCSeq;
    }

    public String getRemoteTarget() {
        return remoteTarget;
    }

    public void setRemoteTarget(String remoteTarget) {
        this.remoteTarget = remoteTarget;
    }

    public String getRemoteUri() {
        return remoteUri;
    }

    public void setRemoteUri(String remoteUri) {
        this.remoteUri = remoteUri;
    }

    public ArrayList<String> getRouteSet() {
        return routeSet;
    }

    public void setRouteSet(ArrayList<String> routeSet) {
        this.routeSet = routeSet;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public String getLocalTag() {
        return localTag;
    }

    public void setLocalTag(String localTag) {
        this.localTag = localTag;
    }

    public String getRemoteTag() {
        return remoteTag;
    }

    public void setRemoteTag(String remoteTag) {
        this.remoteTag = remoteTag;
    }

    public DialogState getState() {
        return state;
    }
    
    
}
