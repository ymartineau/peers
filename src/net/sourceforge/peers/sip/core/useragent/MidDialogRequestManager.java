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

import java.util.Hashtable;

import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderParamName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;
import net.sourceforge.peers.sip.transaction.ClientTransaction;
import net.sourceforge.peers.sip.transaction.ClientTransactionUser;
import net.sourceforge.peers.sip.transaction.Transaction;
import net.sourceforge.peers.sip.transaction.TransactionManager;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;


public class MidDialogRequestManager extends RequestManager
        implements ClientTransactionUser {

    ////////////////////////////////////////////////
    // methods for UAC
    ////////////////////////////////////////////////

    public void generateMidDialogRequest(Dialog dialog,
            String method) {
        

        SipRequest subRequest = dialog.buildSubsequentRequest(RFC3261.METHOD_BYE);

        if (RFC3261.METHOD_BYE.equals(method)) {
            byeHandler.preprocessBye(subRequest, dialog);
        }
        //TODO check that subsequent request is supported before client
        //transaction creation
        if (!RFC3261.METHOD_INVITE.equals(method)) {
            ClientTransaction clientTransaction = createAndStartNonInviteClientTransaction(subRequest);
            if (clientTransaction != null) {
                clientTransaction.start();
            }
        } else {
            //TODO client transaction user is managed by invite handler directly
        }

        
    }
    
    
    protected ClientTransaction createAndStartNonInviteClientTransaction(
            SipRequest sipRequest) {
        //8.1.2
        SipURI requestUri = sipRequest.getRequestUri();

        //TODO if header route is present, addrspec = toproute.nameaddress.addrspec
        String transport = RFC3261.TRANSPORT_UDP;
        Hashtable<String, String> params = requestUri.getUriParameters();
        if (params != null) {
            String reqUriTransport = params.get(RFC3261.PARAM_TRANSPORT);
            if (reqUriTransport != null) {
                transport = reqUriTransport; 
            }
        }
        int port = requestUri.getPort();
        if (port == SipURI.DEFAULT_PORT) {
            port = RFC3261.TRANSPORT_DEFAULT_PORT;
        }
        ClientTransaction clientTransaction = TransactionManager.getInstance()
            .createClientTransaction(sipRequest,
                    requestUri.getHost(), port, transport, this);
        return clientTransaction;
    }
    
    
    
    
    

    
    
    
    
    
    ////////////////////////////////////////////////
    // methods for UAS
    ////////////////////////////////////////////////
    //why static ????
    public static SipResponse generateMidDialogResponse(SipRequest sipRequest,
            Dialog dialog, int statusCode, String reasonPhrase) {
        //8.2.6.2
        SipResponse sipResponse = new SipResponse(statusCode, reasonPhrase);
        SipHeaders requestHeaders = sipRequest.getSipHeaders();
        SipHeaders responseHeaders = sipResponse.getSipHeaders();
        SipHeaderFieldName fromName = new SipHeaderFieldName(RFC3261.HDR_FROM);
        responseHeaders.add(fromName, requestHeaders.get(fromName));
        SipHeaderFieldName callIdName = new SipHeaderFieldName(RFC3261.HDR_CALLID);
        responseHeaders.add(callIdName, requestHeaders.get(callIdName));
        SipHeaderFieldName cseqName = new SipHeaderFieldName(RFC3261.HDR_CSEQ);
        responseHeaders.add(cseqName, requestHeaders.get(cseqName));
        SipHeaderFieldName viaName = new SipHeaderFieldName(RFC3261.HDR_VIA);
        responseHeaders.add(viaName, requestHeaders.get(viaName));//TODO check ordering
        SipHeaderFieldName toName = new SipHeaderFieldName(RFC3261.HDR_TO);
        SipHeaderFieldValue toValue = requestHeaders.get(toName);
        String toTag = toValue.getParam(new SipHeaderParamName(RFC3261.PARAM_TAG));
        if (toTag == null) {
            //TODO generate To tag
            
        } else {
            responseHeaders.add(toName, toValue);
        }
        return sipResponse;
    }

    public void manageMidDialogRequest(SipRequest sipRequest, Dialog dialog) {

        if (dialog.getRemoteCSeq() == Dialog.EMPTY_CSEQ) {
            SipHeaders sipHeaders = sipRequest.getSipHeaders();
            SipHeaderFieldValue cseq =
                sipHeaders.get(new SipHeaderFieldName(RFC3261.HDR_CSEQ));
            String cseqStr = cseq.getValue();
            int pos = cseqStr.indexOf(' ');
            if (pos < 0) {
                pos = cseqStr.indexOf('\t');
            }
            dialog.setRemoteCSeq(Integer.parseInt(cseqStr.substring(0, pos)));
        }
        
        if (RFC3261.METHOD_BYE.equals(sipRequest.getMethod())) {
            byeHandler.handleBye(sipRequest, dialog);
            
        }
    }

    ///////////////////////////////////////
    //ClientTransaction methods
    ///////////////////////////////////////
    public void errResponseReceived(SipResponse sipResponse) {
        // TODO Auto-generated method stub
        
    }


    public void provResponseReceived(SipResponse sipResponse, Transaction transaction) {
        // TODO Auto-generated method stub
        
    }


    public void successResponseReceived(SipResponse sipResponse, Transaction transaction) {
        // TODO Auto-generated method stub
        
    }


    public void transactionTimeout() {
        // TODO Auto-generated method stub
        
    }


    public void transactionTransportError() {
        // TODO Auto-generated method stub
        
    }
}
