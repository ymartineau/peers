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

package net.sourceforge.peers.sip.core.useragent;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.syntaxencoding.NameAddress;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderParamName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transaction.ClientTransaction;
import net.sourceforge.peers.sip.transaction.TransactionManager;
import net.sourceforge.peers.sip.transport.SipRequest;

public class InitialRequestManager extends RequestManager {

    public InitialRequestManager(UserAgent userAgent, TransactionManager
            transactionManager) {
        super(userAgent, transactionManager);
    }

    /**
     * gives a new request outside of a dialog
     * 
     * @param requestUri
     * @param method
     * @return
     * @throws SipUriSyntaxException 
     */
    public SipRequest getGenericRequest(String requestUri, String method,
            String profileUri) throws SipUriSyntaxException {
        //8.1.1
        SipRequest request = new SipRequest(method, new SipURI(requestUri));
        SipHeaders headers = request.getSipHeaders();
        Utils utils = Utils.getInstance();
        //String hostAddress = utils.getMyAddress().getHostAddress();
        
        //Via
        
        //TODO no Via should be added directly by UAC, Via is normally added by Transaction layer
        
//        StringBuffer viaBuf = new StringBuffer();
//        viaBuf.append(RFC3261.DEFAULT_SIP_VERSION);
//        // TODO choose real transport
//        viaBuf.append("/UDP ");
//        viaBuf.append(hostAddress);
//        SipHeaderFieldValue via = new SipHeaderFieldValue(viaBuf.toString());
//        via.addParam(new SipHeaderParamName(RFC3261.PARAM_BRANCHID),
//                utils.generateBranchId());
//        headers.add(new SipHeaderFieldName(RFC3261.HDR_VIA), via);
        

        utils.addCommonHeaders(headers);
        
        //To
        
        NameAddress to = new NameAddress(requestUri);
        headers.add(new SipHeaderFieldName(RFC3261.HDR_TO),
                new SipHeaderFieldValue(to.toString()));
        
        //From
        
        NameAddress fromNA = new NameAddress(profileUri);
        SipHeaderFieldValue from = new SipHeaderFieldValue(fromNA.toString());
        from.addParam(new SipHeaderParamName(RFC3261.PARAM_TAG),
                utils.generateTag());
        headers.add(new SipHeaderFieldName(RFC3261.HDR_FROM), from);
        
        //Call-ID
        
        headers.add(new SipHeaderFieldName(RFC3261.HDR_CALLID),
                new SipHeaderFieldValue(utils.generateCallID()));
        
        //CSeq
        
        headers.add(new SipHeaderFieldName(RFC3261.HDR_CSEQ),
                new SipHeaderFieldValue(utils.generateCSeq(method)));
        
        return request;
    }
 
    public void createInitialRequest(String requestUri, String method,
            String profileUri, String callId) throws SipUriSyntaxException {
        
        SipRequest sipRequest = createInitialRequestStart(requestUri, method,
                profileUri, callId);
        
        ClientTransaction clientTransaction = null;
        if (RFC3261.METHOD_INVITE.equals(method)) {
            clientTransaction = inviteHandler.preProcessInvite(sipRequest);
        }
        
        createInitialRequestEnd(sipRequest, clientTransaction, profileUri);
    }
    
    private SipRequest createInitialRequestStart(String requestUri, String method,
            String profileUri, String callId) throws SipUriSyntaxException {
        SipRequest sipRequest = getGenericRequest(requestUri, method,
                profileUri);
        if (callId != null) {
            SipHeaderFieldValue callIdValue = sipRequest.getSipHeaders().get(
                    new SipHeaderFieldName(RFC3261.HDR_CALLID));
            callIdValue.setValue(callId);
        }
        return sipRequest;
    }
    
    private void createInitialRequestEnd(SipRequest sipRequest,
            ClientTransaction clientTransaction, String profileUri) {
        addContact(sipRequest, clientTransaction.getContact(), profileUri);
        
        // TODO create message receiver on client transport port
        if (clientTransaction != null) {
            clientTransaction.start();
        } else {
            System.err.println("method not supported");
        }
    }
    
    public void createInitialRequest(String requestUri, String method,
            String profileUri) throws SipUriSyntaxException {
        createInitialRequest(requestUri, method, profileUri, null);
    }
    
    public void createCancel(SipRequest inviteRequest,
            MidDialogRequestManager midDialogRequestManager, String profileUri) {
        SipHeaders inviteHeaders = inviteRequest.getSipHeaders();
        SipHeaderFieldValue callId = inviteHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_CALLID));
        SipRequest sipRequest;
        try {
            sipRequest = createInitialRequestStart(
                    inviteRequest.getRequestUri().toString(), RFC3261.METHOD_CANCEL,
                    profileUri, callId.getValue());
        } catch (SipUriSyntaxException e) {
            Logger.getInstance().error(e);
            e.printStackTrace();
            return;
        }
        
        ClientTransaction clientTransaction = null;
            clientTransaction = cancelHandler.preProcessCancel(sipRequest,
                    inviteRequest, midDialogRequestManager);
        if (clientTransaction != null) {
            createInitialRequestEnd(sipRequest, clientTransaction, profileUri);
        }
        
        
    }

    public void manageInitialRequest(SipRequest sipRequest) {
        SipHeaders headers = sipRequest.getSipHeaders();
        
        // TODO authentication
        
        //method inspection
        
        if (!UAS.SUPPORTED_METHODS.contains(sipRequest.getMethod())) {
            //TODO generate 405 (using 8.2.6) with Allow header (20.5) and send it
        }

        
        SipHeaderFieldValue contentType =
            headers.get(new SipHeaderFieldName(RFC3261.HDR_CONTENT_TYPE));
        if (contentType != null) {
            if (!RFC3261.CONTENT_TYPE_SDP.equals(contentType.getValue())) {
                //TODO generate 415 with a Accept header listing supported content types
                //8.2.3
            }
        }

        
        //etc.
        
        
        //TODO create server transaction
        String method = sipRequest.getMethod();
        if (RFC3261.METHOD_INVITE.equals(method)) {
            inviteHandler.handleInitialInvite(sipRequest);
        } else if (RFC3261.METHOD_CANCEL.equals(method)) {
            cancelHandler.handleCancel(sipRequest);
        }
    }

    private void addContact(SipRequest sipRequest, String contactEnd,
            String profileUri) {
        SipHeaders sipHeaders = sipRequest.getSipHeaders();
        
        
        
        //Contact
        
        StringBuffer contactBuf = new StringBuffer();
        contactBuf.append(RFC3261.SIP_SCHEME);
        contactBuf.append(RFC3261.SCHEME_SEPARATOR);
        String userPart = Utils.getInstance().getUserPart(profileUri);
        contactBuf.append(userPart);
        contactBuf.append(RFC3261.AT);
        contactBuf.append(contactEnd);

        NameAddress contactNA = new NameAddress(contactBuf.toString());
        SipHeaderFieldValue contact =
            new SipHeaderFieldValue(contactNA.toString());
        sipHeaders.add(new SipHeaderFieldName(RFC3261.HDR_CONTACT),
                new SipHeaderFieldValue(contact.toString()));
    }

}
