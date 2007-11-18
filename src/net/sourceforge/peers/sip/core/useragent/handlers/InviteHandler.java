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

import gov.nist.jrtp.RtpException;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import net.sourceforge.peers.media.CaptureRtpSender;
import net.sourceforge.peers.media.IncomingRtpReader;
import net.sourceforge.peers.sdp.NoCodecException;
import net.sourceforge.peers.sdp.SDPManager;
import net.sourceforge.peers.sdp.SessionDescription;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderParamName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;
import net.sourceforge.peers.sip.transaction.ClientTransaction;
import net.sourceforge.peers.sip.transaction.ClientTransactionUser;
import net.sourceforge.peers.sip.transaction.ServerTransaction;
import net.sourceforge.peers.sip.transaction.ServerTransactionUser;
import net.sourceforge.peers.sip.transaction.Transaction;
import net.sourceforge.peers.sip.transaction.TransactionManager;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.MessageSender;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;
import net.sourceforge.peers.sip.transport.TransportManager;

public class InviteHandler extends DialogMethodHandler
        implements ServerTransactionUser, ClientTransactionUser {

    //TODO move sdp manager, this should probably be in UserAgent
    private SDPManager sdpManager;
    
    public InviteHandler() {
        sdpManager = new SDPManager();
    }
    
    
    //////////////////////////////////////////////////////////
    // UAS methods
    //////////////////////////////////////////////////////////

    public void handleInitialInvite(SipRequest sipRequest) {
        SipHeaders reqHeaders = sipRequest.getSipHeaders();
        SipHeaderFieldValue contentType =
            reqHeaders.get(new SipHeaderFieldName(RFC3261.HDR_CONTENT_TYPE));
        
        
        if (RFC3261.CONTENT_TYPE_SDP.equals(contentType)) {
            //TODO
//            String sdpResponse;
//            try {
//                sdpResponse = sdpManager.handleOffer(
//                        new String(sipRequest.getBody()));
//            } catch (NoCodecException e) {
//                sdpResponse = sdpManager.generateErrorResponse();
//            }
        } else {
            // TODO manage empty bodies and non-application/sdp content type
        }
        

        //TODO externalize static values 200 and "OK"
        SipResponse sipResponse = buildGenericResponse(sipRequest,
                RFC3261.CODE_200_OK, RFC3261.REASON_200_OK);

        

        Dialog dialog = buildDialogForUas(sipResponse, sipRequest);
        
        // TODO 13.3 dialog invite-specific processing
        
        // TODO timer if there is an Expires header in INVITE
        
        // TODO 1xx
        
        // TODO 3xx
        
        // TODO 486 or 600
        
        String body;
        if (sipRequest.getBody() != null
                && RFC3261.CONTENT_TYPE_SDP.equals(contentType.getValue())) {
            // create response in 200
            try {
                body = sdpManager.handleOffer(sipRequest.getBody());
            } catch (NoCodecException e) {
                body = sdpManager.generateErrorResponse();
            }
        } else {
            // create offer in 200
            body = sdpManager.generateOffer();
        }
        sipResponse.setBody(body.getBytes());
        SipHeaders respHeaders = sipResponse.getSipHeaders();
        respHeaders.add(new SipHeaderFieldName(RFC3261.HDR_CONTENT_TYPE),
                new SipHeaderFieldValue(RFC3261.CONTENT_TYPE_SDP));
        
        
        // TODO determine port and transport for server transaction>transport
        // from initial invite
        // FIXME determine port and transport for server transaction>transport
        ServerTransaction serverTransaction =
            TransactionManager.getInstance().createServerTransaction(sipResponse,
                    RFC3261.TRANSPORT_DEFAULT_PORT, RFC3261.TRANSPORT_UDP, this,
                    sipRequest);
        
        serverTransaction.start();
        
        serverTransaction.receivedRequest(sipRequest);
        
        serverTransaction.sendReponse(sipResponse);
        // TODO manage retransmission of the response (send to the transport)
        // until ACK arrives, if no ACK is received within 64*T1, confirm dialog
        // and terminate it with a BYE
        
        dialog.receivedOrSent2xx();
        
        List<Dialog> dialogs = UserAgent.getInstance().getDialogs();
        if (!dialogs.contains(dialog)) {
            dialogs.add(dialog);
            System.out.println("added dialog " + dialog.getId());
        }
        
        List<String> peers = UserAgent.getInstance().getPeers();
        String responseTo = reqHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_FROM)).getValue();
        if (!peers.contains(responseTo)) {
            peers.add(responseTo);
        }
    }
    
    public void handleReInvite(SipRequest sipRequest) {
        
    }
    
    
    //////////////////////////////////////////////////////////
    // UAC methods
    //////////////////////////////////////////////////////////
    
    public ClientTransaction preProcessInvite(SipRequest sipRequest) {
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
            .createClientTransaction(sipRequest, requestUri.getHost(),
                    port, transport, this);
        sipRequest.setBody(sdpManager.generateOffer().getBytes());
        SipHeaders respHeaders = sipRequest.getSipHeaders();
        respHeaders.add(new SipHeaderFieldName(RFC3261.HDR_CONTENT_TYPE),
                new SipHeaderFieldValue(RFC3261.CONTENT_TYPE_SDP));
        return clientTransaction;
    }
    
    public void preProcessReInvite(SipRequest sipRequest) {
        //TODO
    }

    //////////////////////////////////////////////////////////
    // ClientTransactionUser methods
    //////////////////////////////////////////////////////////

    public void errResponseReceived(SipResponse sipResponse) {
        // TODO Auto-generated method stub
        
    }

    public void provResponseReceived(SipResponse sipResponse, Transaction transaction) {
        // TODO Auto-generated method stub
        
    }

    public void successResponseReceived(SipResponse sipResponse, Transaction transaction) {
        SipHeaders responseHeaders = sipResponse.getSipHeaders();
        String cseq = responseHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_CSEQ)).getValue();
        String method = cseq.substring(cseq.trim().lastIndexOf(' ') + 1);
        if (!RFC3261.METHOD_INVITE.equals(method)) {
            return;
        }
        
        
        
        
        
        
        
        
        //13.2.2.4

        List<String> peers = UserAgent.getInstance().getPeers();
        String responseTo = responseHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_TO)).getValue();
        if (!peers.contains(responseTo)) {
            peers.add(responseTo);
            //timer used to purge dialogs which are not confirmed
            //after a given time
            ackTimer.schedule(new AckTimerTask(responseTo),
                    64 * RFC3261.TIMER_T1);
        }
        
        Dialog dialog = DialogManager.getInstance().getDialog(sipResponse);
        
        if (dialog != null) {
            //dialog already created with a 180 for example
            dialog.setRouteSet(computeRouteSet(sipResponse.getSipHeaders()));
        } else {
            //new dialog
            dialog = buildDialogForUac(sipResponse, transaction);
        }
        
        UserAgent.getInstance().getDialogs().add(dialog);
        System.out.println("added dialog " + dialog.getId());
        
        //added for media
        SessionDescription sessionDescription =
            sdpManager.handleAnswer(sipResponse.getBody());
        String remoteAddress = sessionDescription.getIpAddress().getHostAddress();
        int remotePort = sessionDescription.getMedias().get(0).getPort();
        String localAddress = Utils.getInstance().getMyAddress().getHostAddress();
        CaptureRtpSender captureRtpSender;
        try {
            captureRtpSender = new CaptureRtpSender(localAddress, 6000,
                    remoteAddress, remotePort);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        UserAgent.getInstance().setCaptureRtpSender(captureRtpSender);
        try {
            captureRtpSender.start();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        IncomingRtpReader incomingRtpReader;
        try {
            //TODO retrieve port from SDP offer
//            incomingRtpReader = new IncomingRtpReader(localAddress, 6000,
//                    remoteAddress, remotePort);
            incomingRtpReader = new IncomingRtpReader(captureRtpSender.getRtpSession());
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }
        UserAgent.getInstance().setIncomingRtpReader(incomingRtpReader);
        try {
            incomingRtpReader.start();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (RtpException e1) {
            e1.printStackTrace();
        }
        
        /////////////////
        
        //switch to confirmed state
        dialog.receivedOrSent2xx();
        
        //generate ack
        //p. 82 §3
        SipRequest ack = dialog.buildSubsequentRequest(RFC3261.METHOD_ACK);
        
        
        //update CSeq
        
        SipHeaders ackHeaders = ack.getSipHeaders();
        SipHeaderFieldName cseqName = new SipHeaderFieldName(RFC3261.HDR_CSEQ);
        SipHeaderFieldValue ackCseq = ackHeaders.get(cseqName);
        
        SipRequest request = transaction.getRequest();
        SipHeaders requestHeaders = request.getSipHeaders();
        SipHeaderFieldValue requestCseq = requestHeaders.get(cseqName);
        
        ackCseq.setValue(requestCseq.toString().replace(RFC3261.METHOD_INVITE, RFC3261.METHOD_ACK));
        
        //add Via with only the branchid parameter
        
        SipHeaderFieldValue via = new SipHeaderFieldValue("");
        SipHeaderFieldValue respTopVia =Utils.getInstance().getTopVia(sipResponse);
        SipHeaderParamName branchIdName = new SipHeaderParamName(RFC3261.PARAM_BRANCH);
        via.addParam(branchIdName, respTopVia.getParam(branchIdName));
        
        ackHeaders.add(new SipHeaderFieldName(RFC3261.HDR_VIA), via, 0);
        
        //TODO authentication headers
        
        if (request.getBody() == null && sipResponse.getBody() != null) {
            //TODO add a real SDP answer
            ack.setBody(sipResponse.getBody());
        }

        //TODO check if sdp is acceptable

        SipURI requestUri = ack.getRequestUri();

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

        try {
            MessageSender sender = TransportManager.getInstance().createClientTransport(
                    ack, requestUri.getHost(), port, transport);
            sender.sendMessage(ack);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
        
        
        
        
    }

    public void transactionTimeout() {
        // TODO Auto-generated method stub
        
    }

    public void transactionTransportError() {
        // TODO Auto-generated method stub
        
    }
    
    //////////////////////////////////////////////////////////
    // ServerTransactionUser methods
    //////////////////////////////////////////////////////////
    
    public void transactionFailure() {
        // TODO manage transaction failure (ACK was not received)
        
    }
    

}
