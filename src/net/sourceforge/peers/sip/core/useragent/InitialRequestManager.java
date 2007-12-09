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

package net.sourceforge.peers.sip.core.useragent;

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
import net.sourceforge.peers.sip.transport.SipRequest;

public class InitialRequestManager extends RequestManager {

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
            String profileUri) throws SipUriSyntaxException {
        SipRequest sipRequest = getGenericRequest(requestUri, method,
                profileUri);
        ClientTransaction clientTransaction = null;
        if (RFC3261.METHOD_INVITE.equals(method)) {
            clientTransaction = inviteHandler.preProcessInvite(sipRequest);
        }
        addContact(sipRequest, clientTransaction.getContact());
        
        // TODO create message receiver on client transport port
        if (clientTransaction != null) {
            clientTransaction.start();
        } else {
            System.err.println("method not supported");
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
        if (RFC3261.METHOD_INVITE.equals(sipRequest.getMethod())) {
            inviteHandler.handleInitialInvite(sipRequest);
            
        }
    }

    private void addContact(SipRequest sipRequest, String contactEnd) {
        SipHeaders sipHeaders = sipRequest.getSipHeaders();
        
        
        
        //Contact
        
        StringBuffer contactBuf = new StringBuffer();
        contactBuf.append(RFC3261.SIP_SCHEME);
        contactBuf.append(RFC3261.SCHEME_SEPARATOR);
        String userPart = Utils.getInstance().getUserPart(
                UAC.getInstance().getProfileUri());
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
