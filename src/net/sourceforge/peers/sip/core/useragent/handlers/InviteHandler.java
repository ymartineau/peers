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

package net.sourceforge.peers.sip.core.useragent.handlers;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.media.CaptureRtpSender;
import net.sourceforge.peers.media.Echo;
import net.sourceforge.peers.media.IncomingRtpReader;
import net.sourceforge.peers.media.SoundManager;
import net.sourceforge.peers.sdp.MediaDestination;
import net.sourceforge.peers.sdp.NoCodecException;
import net.sourceforge.peers.sdp.SessionDescription;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.MidDialogRequestManager;
import net.sourceforge.peers.sip.core.useragent.RequestManager;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.NameAddress;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderParamName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;
import net.sourceforge.peers.sip.transaction.ClientTransaction;
import net.sourceforge.peers.sip.transaction.ClientTransactionUser;
import net.sourceforge.peers.sip.transaction.InviteClientTransaction;
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

    private MediaDestination mediaDestination;
    
    public InviteHandler(UserAgent userAgent,
            DialogManager dialogManager,
            TransactionManager transactionManager,
            TransportManager transportManager) {
        super(userAgent, dialogManager, transactionManager, transportManager);
    }
    
    
    //////////////////////////////////////////////////////////
    // UAS methods
    //////////////////////////////////////////////////////////

    public void handleInitialInvite(SipRequest sipRequest) {
        
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

        SipListener sipListener = userAgent.getSipListener();
        if (sipListener != null) {
            sipListener.incomingCall(sipRequest, sipResponse);
        }

        dialog.receivedOrSent1xx();

        List<String> peers = userAgent.getPeers();
        String responseTo = sipRequest.getSipHeaders().get(
                new SipHeaderFieldName(RFC3261.HDR_FROM)).getValue();
        if (!peers.contains(responseTo)) {
            peers.add(responseTo);
        }
        
    }
    
    public void handleReInvite(SipRequest sipRequest, Dialog dialog) {
        SipHeaders sipHeaders = sipRequest.getSipHeaders();

        // 12.2.2 update dialog
        SipHeaderFieldValue contact =
            sipHeaders.get(new SipHeaderFieldName(RFC3261.HDR_CONTACT));
        if (contact != null) {
            String contactStr = contact.getValue();
            if (contactStr.indexOf(RFC3261.LEFT_ANGLE_BRACKET) > -1) {
                contactStr = NameAddress.nameAddressToUri(contactStr);
            }
            dialog.setRemoteTarget(contactStr);
        }


        // update session
        sendSuccessfulResponse(sipRequest, dialog);
        
    }

    private void sendSuccessfulResponse(SipRequest sipRequest, Dialog dialog) {
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
        byte[] offer = sipRequest.getBody();
        if (offer != null && RFC3261.CONTENT_TYPE_SDP.equals(
                contentType.getValue())) {
            // create response in 200
            try {
                mediaDestination = sdpManager.getMediaDestination(offer);
                body = sdpManager.generateResponse(offer);
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
        
        ArrayList<String> routeSet = dialog.getRouteSet();
        if (routeSet != null) {
            SipHeaderFieldName recordRoute = new SipHeaderFieldName(RFC3261.HDR_RECORD_ROUTE);
            for (String route : routeSet) {
                respHeaders.add(recordRoute, new SipHeaderFieldValue(route));
            }
        }
        
        // TODO determine port and transport for server transaction>transport
        // from initial invite
        // FIXME determine port and transport for server transaction>transport
        ServerTransaction serverTransaction = transactionManager
                .getServerTransaction(sipRequest);
        if (serverTransaction == null) {
            // in re-INVITE case, no serverTransaction has been created
            serverTransaction = (InviteServerTransaction)
            transactionManager.createServerTransaction(sipResponse,
                    userAgent.getSipPort(), RFC3261.TRANSPORT_UDP, this,
                    sipRequest);
        }
        serverTransaction.start();
        
        serverTransaction.receivedRequest(sipRequest);
        
        serverTransaction.sendReponse(sipResponse);
        // TODO manage retransmission of the response (send to the transport)
        // until ACK arrives, if no ACK is received within 64*T1, confirm dialog
        // and terminate it with a BYE

//        Logger.getInstance().debug("before dialog.receivedOrSent2xx();");
//        Logger.getInstance().debug("dialog state: " + dialog.getState());
    }

    public void acceptCall(SipRequest sipRequest, Dialog dialog) {
        sendSuccessfulResponse(sipRequest, dialog);
        
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
        SipHeaders requestHeaders = sipRequest.getSipHeaders();
        SipURI destinationUri = RequestManager.getDestinationUri(sipRequest);

        //TODO if header route is present, addrspec = toproute.nameaddress.addrspec

        String transport = RFC3261.TRANSPORT_UDP;
        Hashtable<String, String> params = destinationUri.getUriParameters();
        if (params != null) {
            String reqUriTransport = params.get(RFC3261.PARAM_TRANSPORT);
            if (reqUriTransport != null) {
                transport = reqUriTransport; 
            }
        }
        int port = destinationUri.getPort();
        if (port == SipURI.DEFAULT_PORT) {
            port = RFC3261.TRANSPORT_DEFAULT_PORT;
        }
        ClientTransaction clientTransaction = transactionManager
                .createClientTransaction(sipRequest, destinationUri.getHost(),
                    port, transport, null, this);
        sipRequest.setBody(sdpManager.generateOffer().getBytes());
        requestHeaders.add(new SipHeaderFieldName(RFC3261.HDR_CONTENT_TYPE),
                new SipHeaderFieldValue(RFC3261.CONTENT_TYPE_SDP));
        return clientTransaction;
    }
    
    public void preProcessReInvite(SipRequest sipRequest) {
        //TODO
    }

    //////////////////////////////////////////////////////////
    // ClientTransactionUser methods
    //////////////////////////////////////////////////////////

    public void errResponseReceived(final SipResponse sipResponse) {
        Dialog dialog = dialogManager.getDialog(sipResponse);
        if (dialog != null) {
            dialog.receivedOrSent300To699();
        }
        int statusCode = sipResponse.getStatusCode();
        if (statusCode == RFC3261.CODE_401_UNAUTHORIZED
                || statusCode == RFC3261.CODE_407_PROXY_AUTHENTICATION_REQUIRED
                && !challenged) {
            InviteClientTransaction inviteClientTransaction =
                (InviteClientTransaction)
                transactionManager.getClientTransaction(sipResponse);
            final SipRequest sipRequest = inviteClientTransaction.getRequest();
            // try to send invite 1 second later, ugly solution,
            // but it seems to work...
            Timer timer = new Timer();
            TimerTask authInviteTask = new TimerTask() {
                @Override
                public void run() {
                    if (challengeManager != null) {
                        challengeManager.handleChallenge(sipRequest,
                                sipResponse);
                    }
                }
            };
            timer.schedule(authInviteTask, 1500);
            challenged = true;
            return;
        } else {
            challenged = false;
        }
        SipListener sipListener = userAgent.getSipListener();
        if (sipListener != null) {
            sipListener.error(sipResponse);
        }

    }

    public void provResponseReceived(SipResponse sipResponse, Transaction transaction) {
        // dialog may have already been created if a previous 1xx has
        // already been received
        Dialog dialog = dialogManager.getDialog(sipResponse);
        boolean isFirstProvRespWithToTag = false;
        if (dialog == null) {
            SipHeaderFieldValue to = sipResponse.getSipHeaders().get(
                    new SipHeaderFieldName(RFC3261.HDR_TO));
            String toTag = to.getParam(new SipHeaderParamName(RFC3261.PARAM_TAG));
            if (toTag != null) {
                dialog = dialogManager.createDialog(sipResponse);
                isFirstProvRespWithToTag = true;
            } else {
                //TODO maybe stop retransmissions
            }
        }
        
        if (dialog != null) {
            buildOrUpdateDialogForUac(sipResponse, transaction);
        }
        
//        
//        if (dialog == null && sipResponse.getStatusCode() != RFC3261.CODE_100_TRYING) {
//            Logger.debug("dialog not found for prov response");
//            isFirstProvRespWithToTag = true;
//            SipHeaderFieldValue to = sipResponse.getSipHeaders()
//                .get(new SipHeaderFieldName(RFC3261.HDR_TO));
//            String toTag = to.getParam(new SipHeaderParamName(RFC3261.PARAM_TAG));
//            if (toTag != null) {
//                dialog = buildOrUpdateDialogForUac(sipResponse, transaction);
//            }
//        }
        //TODO this notification is probably useless because dialog state modification
        //     thereafter always notify dialog observers
        if (isFirstProvRespWithToTag) {
            SipListener sipListener = userAgent.getSipListener();
            if (sipListener != null) {
                sipListener.ringing(sipResponse);
            }
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
        
        challenged = false;
        
        
        
        
        
        
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
        }
        dialog = buildOrUpdateDialogForUac(sipResponse, transaction);
        
        SipListener sipListener = userAgent.getSipListener();
        if (sipListener != null) {
            sipListener.calleePickup(sipResponse);
        }

        //added for media
        SessionDescription sessionDescription =
            sdpManager.handleAnswer(sipResponse.getBody());
        String remoteAddress = sessionDescription.getIpAddress().getHostAddress();
        int remotePort = sessionDescription.getMedias().get(0).getPort();
        String localAddress = userAgent.getMyAddress().getHostAddress();

        switch (userAgent.getMediaMode()) {
        case captureAndPlayback:
            CaptureRtpSender captureRtpSender;
            //TODO this could be optimized, create captureRtpSender at stack init
            //     and just retrieve it here
            SoundManager soundManager = userAgent.getSoundManager();
            soundManager.openAndStartLines();
            try {
                captureRtpSender = new CaptureRtpSender(localAddress,
                        userAgent.getRtpPort(),
                        remoteAddress, remotePort, soundManager,
                        userAgent.isMediaDebug());
            } catch (IOException e) {
                Logger.error("input/output error", e);
                return;
            }
            userAgent.setCaptureRtpSender(captureRtpSender);

            try {
                captureRtpSender.start();
            } catch (IOException e) {
                Logger.error("input/output error", e);
            }
            
            IncomingRtpReader incomingRtpReader;
            try {
                //TODO retrieve port from SDP offer
//                    incomingRtpReader = new IncomingRtpReader(localAddress,
//                            Utils.getInstance().getRtpPort(),
//                            remoteAddress, remotePort);
                incomingRtpReader = new IncomingRtpReader(
                        captureRtpSender.getRtpSession(), soundManager);
            } catch (IOException e) {
                Logger.error("input/output error", e);
                return;
            }
            userAgent.setIncomingRtpReader(incomingRtpReader);

            incomingRtpReader.start();
            break;

        case echo:
            Echo echo;
            try {
                echo = new Echo(localAddress, userAgent.getRtpPort(),
                            remoteAddress, remotePort);
            } catch (UnknownHostException e) {
                Logger.error("unknown host amongst "
                        + localAddress + " or " + remoteAddress);
                return;
            }
            userAgent.setEcho(echo);
            Thread echoThread = new Thread(echo);
            echoThread.start();
            break;
        case none:
        default:
            break;
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

        SipURI destinationUri = RequestManager.getDestinationUri(ack);

        //TODO if header route is present, addrspec = toproute.nameaddress.addrspec
        
        String transport = RFC3261.TRANSPORT_UDP;
        Hashtable<String, String> params = destinationUri.getUriParameters();
        if (params != null) {
            String reqUriTransport = params.get(RFC3261.PARAM_TRANSPORT);
            if (reqUriTransport != null) {
                transport = reqUriTransport; 
            }
        }
        int port = destinationUri.getPort();
        if (port == SipURI.DEFAULT_PORT) {
            port = RFC3261.TRANSPORT_DEFAULT_PORT;
        }

        try {
            //FIXME remote address can be a proxy
            MessageSender sender = transportManager.createClientTransport(
                    ack, destinationUri.getHost(), port, transport);
            sender.sendMessage(ack);
        } catch (IOException e) {
            Logger.error("input/output error", e);
        }
        
        
        
        
        
        
    }

    public void handleAck(SipRequest ack, Dialog dialog) {
        // TODO determine if ACK is ACK of an initial INVITE or a re-INVITE
        // in first case, captureRtpSender and incomingRtpReader must be
        // created, in the second case, they must be updated.
        Logger.debug("handleAck");

        String destAddress = mediaDestination.getDestination();
        int destPort = mediaDestination.getPort();
        
        switch (userAgent.getMediaMode()) {
        case captureAndPlayback:
            //TODO this could be optimized, create captureRtpSender at stack init
            //     and just retrieve it here
            CaptureRtpSender captureRtpSender;
            captureRtpSender = userAgent.getCaptureRtpSender();
            IncomingRtpReader incomingRtpReader =
                userAgent.getIncomingRtpReader();
            if (incomingRtpReader != null) {
                incomingRtpReader.stop();
            }
            if (captureRtpSender != null) {
                captureRtpSender.stop();
                while (!captureRtpSender.isTerminated()) {
                    try {
                        Thread.sleep(15);
                    } catch (InterruptedException e) {
                        Logger.debug("sleep interrupted");
                    }
                }
            }
            SoundManager soundManager = userAgent.getSoundManager();
            soundManager.closeLines();
            soundManager.openAndStartLines();
            try {
                captureRtpSender = new CaptureRtpSender(
                        userAgent.getMyAddress().getHostAddress(),
                        userAgent.getRtpPort(), destAddress, destPort,
                        soundManager, userAgent.isMediaDebug());
            } catch (IOException e) {
                Logger.error("input/output error", e);
                return;
            }
            userAgent.setCaptureRtpSender(captureRtpSender);
            try {
                captureRtpSender.start();
            } catch (IOException e) {
                Logger.error("input/output error", e);
            }
            try {
                //TODO retrieve port from SDP offer
//                        incomingRtpReader = new IncomingRtpReader(localAddress,
//                                Utils.getInstance().getRtpPort(),
//                                remoteAddress, remotePort);
                //FIXME RTP sessions can be different !
                incomingRtpReader = new IncomingRtpReader(
                        captureRtpSender.getRtpSession(), soundManager);
            } catch (IOException e) {
                Logger.error("input/output error", e);
                return;
            }
            userAgent.setIncomingRtpReader(incomingRtpReader);

            incomingRtpReader.start();
            break;
        case echo:
            Echo echo;
            try {
                echo = new Echo(userAgent.getMyAddress().getHostAddress(),
                        userAgent.getRtpPort(), destAddress, destPort);
            } catch (UnknownHostException e) {
                Logger.error("unknown host amongst "
                        + userAgent.getMyAddress().getHostAddress()
                        + " or " + destAddress);
                return;
            }
            userAgent.setEcho(echo);
            Thread echoThread = new Thread(echo);
            echoThread.start();
            break;
        case none:
        default:
            break;
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
