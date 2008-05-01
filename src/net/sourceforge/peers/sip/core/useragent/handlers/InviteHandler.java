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

package net.sourceforge.peers.sip.core.useragent.handlers;

import gov.nist.jrtp.RtpException;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.media.CaptureRtpSender;
import net.sourceforge.peers.media.IncomingRtpReader;
import net.sourceforge.peers.sdp.NoCodecException;
import net.sourceforge.peers.sdp.SDPManager;
import net.sourceforge.peers.sdp.SessionDescription;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.MidDialogRequestManager;
import net.sourceforge.peers.sip.core.useragent.SipEvent;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.core.useragent.SipEvent.EventType;
import net.sourceforge.peers.sip.syntaxencoding.NameAddress;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderParamName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;
import net.sourceforge.peers.sip.transaction.ClientTransaction;
import net.sourceforge.peers.sip.transaction.ClientTransactionUser;
import net.sourceforge.peers.sip.transaction.InviteServerTransaction;
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
    
    public InviteHandler(UserAgent userAgent,
            DialogManager dialogManager,
            TransactionManager transactionManager,
            TransportManager transportManager) {
        super(userAgent, dialogManager, transactionManager, transportManager);
        sdpManager = new SDPManager(userAgent);
    }
    
    
    //////////////////////////////////////////////////////////
    // UAS methods
    //////////////////////////////////////////////////////////

    public void handleInitialInvite(SipRequest sipRequest) {
        
//        setChanged();
//        ArrayList args = new ArrayList();
//        args.add(Event.incomingCall);
//        args.add(sipRequest);
//        
//        notifyObservers(args);
        
        //generate 180 Ringing
        SipResponse sipResponse = buildGenericResponse(sipRequest,
                RFC3261.CODE_180_RINGING, RFC3261.REASON_180_RINGING);
        Dialog dialog = buildDialogForUas(sipResponse, sipRequest);
        //here dialog is already stored in dialogs in DialogManager
        
        InviteServerTransaction inviteServerTransaction = (InviteServerTransaction)
            transactionManager.createServerTransaction(sipResponse,
                    userAgent.getSipPort(), RFC3261.TRANSPORT_UDP, this,
                    sipRequest);
        
        inviteServerTransaction.start();
        
        inviteServerTransaction.receivedRequest(sipRequest);
        
        //TODO send 180 more than once
        inviteServerTransaction.sendReponse(sipResponse);

        setChanged();
        notifyObservers(new SipEvent(EventType.INCOMING_CALL, sipResponse));

        dialog.receivedOrSent1xx();

        List<String> peers = userAgent.getPeers();
        String responseTo = sipRequest.getSipHeaders().get(
                new SipHeaderFieldName(RFC3261.HDR_FROM)).getValue();
        if (!peers.contains(responseTo)) {
            peers.add(responseTo);
        }
        
    }
    
    public void handleReInvite(SipRequest sipRequest) {
        
    }
    
    //FIXME remove useragent parameter (redesign interface with gui)
    public void acceptCall(SipRequest sipRequest, Dialog dialog,
            UserAgent userAgent) {
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


        //TODO if mode autoanswer just send 200 without asking any question
        SipResponse sipResponse =
            MidDialogRequestManager.generateMidDialogResponse(
                    sipRequest,
                    dialog,
                    RFC3261.CODE_200_OK,
                    RFC3261.REASON_200_OK);
        
        // TODO 13.3 dialog invite-specific processing
        
        // TODO timer if there is an Expires header in INVITE
        
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
        ServerTransaction serverTransaction = transactionManager
                .getServerTransaction(sipRequest);
        
        serverTransaction.start();
        
        serverTransaction.receivedRequest(sipRequest);
        
        serverTransaction.sendReponse(sipResponse);
        // TODO manage retransmission of the response (send to the transport)
        // until ACK arrives, if no ACK is received within 64*T1, confirm dialog
        // and terminate it with a BYE

//        Logger.getInstance().debug("before dialog.receivedOrSent2xx();");
//        Logger.getInstance().debug("dialog state: " + dialog.getState());
        
        dialog.receivedOrSent2xx();
//        Logger.getInstance().debug("dialog state: " + dialog.getState());
//        Logger.getInstance().debug("after dialog.receivedOrSent2xx();");
        
//        setChanged();
//        notifyObservers(sipRequest);
    }
    
    public void rejectCall(SipRequest sipRequest) {
        //TODO generate 486, etc.
        SipHeaders reqHeaders = sipRequest.getSipHeaders();
        SipHeaderFieldValue to = reqHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_FROM));
        String remoteUri = to.getValue();
        if (remoteUri.indexOf(RFC3261.LEFT_ANGLE_BRACKET) > -1) {
            remoteUri = NameAddress.nameAddressToUri(remoteUri);
        }
        Dialog dialog = dialogManager.getDialog(remoteUri);
        
        //TODO manage auto reject Do not disturb (DND)
        SipResponse sipResponse =
            MidDialogRequestManager.generateMidDialogResponse(
                    sipRequest,
                    dialog,
                    RFC3261.CODE_486_BUSYHERE,
                    RFC3261.REASON_486_BUSYHERE);
        
        // TODO determine port and transport for server transaction>transport
        // from initial invite
        // FIXME determine port and transport for server transaction>transport
        ServerTransaction serverTransaction = transactionManager
                .getServerTransaction(sipRequest);
        
        serverTransaction.start();
        
        serverTransaction.receivedRequest(sipRequest);
        
        serverTransaction.sendReponse(sipResponse);
        
        dialog.receivedOrSent300To699();
        
//        setChanged();
//        notifyObservers(sipRequest);
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
        ClientTransaction clientTransaction = transactionManager
                .createClientTransaction(sipRequest, requestUri.getHost(),
                    port, transport, null, this, transportManager);
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
        Dialog dialog = dialogManager.getDialog(sipResponse);
        if (dialog != null) {
            dialog.receivedOrSent300To699();
        }
        setChanged();
        notifyObservers(new SipEvent(EventType.ERROR, sipResponse));
    }

    public void provResponseReceived(SipResponse sipResponse, Transaction transaction) {
        // dialog may have already been created if a previous 1xx has
        // already been received
        Dialog dialog = dialogManager.getDialog(sipResponse);
        boolean isFirstProvResp = false;
        if (dialog == null && sipResponse.getStatusCode() != RFC3261.CODE_100_TRYING) {
            Logger.debug("dialog not found for prov response");
            isFirstProvResp = true;
            SipHeaderFieldValue to = sipResponse.getSipHeaders()
                .get(new SipHeaderFieldName(RFC3261.HDR_TO));
            String toTag = to.getParam(new SipHeaderParamName(RFC3261.PARAM_TAG));
            if (toTag != null) {
                dialog = buildDialogForUac(sipResponse, transaction);
            }
        }
        //TODO this notification is probably useless because dialog state modification
        //     thereafter always notify dialog observers
        if (isFirstProvResp) {
            setChanged();
            notifyObservers(new SipEvent(EventType.RINGING, sipResponse));
            dialog.receivedOrSent1xx();
        }
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

        List<String> peers = userAgent.getPeers();
        String responseTo = responseHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_TO)).getValue();
        if (!peers.contains(responseTo)) {
            peers.add(responseTo);
            //timer used to purge dialogs which are not confirmed
            //after a given time
            ackTimer.schedule(new AckTimerTask(responseTo),
                    64 * RFC3261.TIMER_T1);
        }
        
        Dialog dialog = dialogManager.getDialog(sipResponse);
        
        if (dialog != null) {
            //dialog already created with a 180 for example
            dialog.setRouteSet(computeRouteSet(sipResponse.getSipHeaders()));
        } else {
            //new dialog
            dialog = buildDialogForUac(sipResponse, transaction);
        }
        
        setChanged();
        notifyObservers(new SipEvent(EventType.CALLEE_PICKUP, sipResponse));
        
        //added for media
        SessionDescription sessionDescription =
            sdpManager.handleAnswer(sipResponse.getBody());
        String remoteAddress = sessionDescription.getIpAddress().getHostAddress();
        int remotePort = sessionDescription.getMedias().get(0).getPort();
        String localAddress = userAgent.getMyAddress().getHostAddress();
        CaptureRtpSender captureRtpSender;
        //TODO this could be optimized, create captureRtpSender at stack init
        //     and just retrieve it here
        try {
            captureRtpSender = new CaptureRtpSender(localAddress,
                    userAgent.getRtpPort(),
                    remoteAddress, remotePort);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        userAgent.setCaptureRtpSender(captureRtpSender);

        try {
            captureRtpSender.start();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        IncomingRtpReader incomingRtpReader;
        try {
            //TODO retrieve port from SDP offer
//                incomingRtpReader = new IncomingRtpReader(localAddress,
//                        Utils.getInstance().getRtpPort(),
//                        remoteAddress, remotePort);
            incomingRtpReader = new IncomingRtpReader(captureRtpSender.getRtpSession());
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }
        userAgent.setIncomingRtpReader(incomingRtpReader);

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
        SipHeaderFieldValue respTopVia = Utils.getTopVia(sipResponse);
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
            MessageSender sender = transportManager.createClientTransport(
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
