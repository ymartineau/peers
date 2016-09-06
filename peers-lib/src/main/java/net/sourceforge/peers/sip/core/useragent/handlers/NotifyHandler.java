package net.sourceforge.peers.sip.core.useragent.handlers;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sdp.MediaDestination;
import net.sourceforge.peers.sdp.NoCodecException;
import net.sourceforge.peers.sdp.SDPManager;
import net.sourceforge.peers.sdp.SessionDescription;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.core.useragent.RequestManager;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.transaction.ClientTransaction;
import net.sourceforge.peers.sip.transaction.ClientTransactionUser;
import net.sourceforge.peers.sip.transaction.InviteServerTransaction;
import net.sourceforge.peers.sip.transaction.NonInviteServerTransaction;
import net.sourceforge.peers.sip.transaction.ServerTransaction;
import net.sourceforge.peers.sip.transaction.ServerTransactionUser;
import net.sourceforge.peers.sip.transaction.Transaction;
import net.sourceforge.peers.sip.transaction.TransactionManager;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;
import net.sourceforge.peers.sip.transport.TransportManager;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

public class NotifyHandler extends DialogMethodHandler implements ServerTransactionUser, ClientTransactionUser {

    public static final int TIMEOUT = 100;
    private MediaDestination mediaDestination;

    public NotifyHandler(UserAgent userAgent, DialogManager dialogManager, TransactionManager transactionManager, TransportManager transportManager, Logger logger) {
        super(userAgent, dialogManager, transactionManager, transportManager, logger);
    }

    public void handleNotify(SipRequest sipRequest) {
        sendSuccessfulResponse(sipRequest, dialogManager.getDialog(sipRequest));
    }

    //#################### copied from invite handler #########################

    private synchronized void sendSuccessfulResponse(SipRequest sipRequest, Dialog dialog) {
        sdpManager = new SDPManager(userAgent, logger);
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
                RequestManager.generateResponse(
                        sipRequest,
                        dialog,
                        RFC3261.CODE_200_OK,
                        RFC3261.REASON_200_OK);

        // TODO 13.3 dialog invite-specific processing

        // TODO timer if there is an Expires header in INVITE

        // TODO 3xx

        // TODO 486 or 600

        byte[] offerBytes = sipRequest.getBody();
        SessionDescription answer;
        try {
            DatagramSocket datagramSocket = getDatagramSocket();

            if (offerBytes != null && contentType != null &&
                    RFC3261.CONTENT_TYPE_SDP.equals(contentType.getValue())) {
                // create response in 200
                try {
                    SessionDescription offer = sdpManager.parse(offerBytes);
                    answer = sdpManager.createSessionDescription(offer,
                            datagramSocket.getLocalPort());
                    mediaDestination = sdpManager.getMediaDestination(offer);
                } catch (NoCodecException e) {
                    answer = sdpManager.createSessionDescription(null,
                            datagramSocket.getLocalPort());
                }
            } else {
                // create offer in 200 (never tested...)
                answer = sdpManager.createSessionDescription(null,
                        datagramSocket.getLocalPort());
            }
            sipResponse.setBody(answer.toString().getBytes());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        SipHeaders respHeaders = sipResponse.getSipHeaders();
        respHeaders.add(new SipHeaderFieldName(RFC3261.HDR_CONTENT_TYPE),
                new SipHeaderFieldValue(RFC3261.CONTENT_TYPE_SDP));

        // TODO determine port and transport for server transaction>transport
        // from initial invite
        // FIXME determine port and transport for server transaction>transport
        ServerTransaction serverTransaction = transactionManager
                .getServerTransaction(sipRequest);
        if (serverTransaction == null) {
            // in re-INVITE case, no serverTransaction has been created
            serverTransaction = transactionManager.createServerTransaction(sipResponse,
                            userAgent.getSipPort(), RFC3261.TRANSPORT_UDP, this,
                            sipRequest);
        }
        serverTransaction.start();

        serverTransaction.receivedRequest(sipRequest);

        serverTransaction.sendReponse(sipResponse);
        // TODO manage retransmission of the response (send to the transport)
        // until ACK arrives, if no ACK is received within 64*T1, confirm dialog
        // and terminate it with a BYE

//        logger.getInstance().debug("before dialog.receivedOrSent2xx();");
//        logger.getInstance().debug("dialog state: " + dialog.getState());
    }

    private DatagramSocket getDatagramSocket() {
        DatagramSocket datagramSocket = userAgent.getMediaManager()
                .getDatagramSocket();
        if (datagramSocket == null) { // initial invite success response
            // AccessController.doPrivileged added for plugin compatibility
            datagramSocket = AccessController.doPrivileged(
                    new PrivilegedAction<DatagramSocket>() {

                        @Override
                        public DatagramSocket run() {
                            DatagramSocket datagramSocket = null;
                            int rtpPort = userAgent.getConfig().getRtpPort();
                            try {
                                if (rtpPort == 0) {
                                    int localPort = -1;
                                    while (localPort % 2 != 0) {
                                        datagramSocket = new DatagramSocket();
                                        localPort = datagramSocket.getLocalPort();
                                        if (localPort % 2 != 0) {
                                            datagramSocket.close();
                                        }
                                    }
                                } else {
                                    datagramSocket = new DatagramSocket(rtpPort);
                                }
                            } catch (SocketException e) {
                                logger.error("cannot create datagram socket ", e);
                            }

                            return datagramSocket;
                        }
                    }
            );
            logger.debug("new rtp DatagramSocket " + datagramSocket.hashCode());
            try {
                datagramSocket.setSoTimeout(TIMEOUT);
            } catch (SocketException e) {
                logger.error("cannot set timeout on datagram socket ", e);
            }
            userAgent.getMediaManager().setDatagramSocket(datagramSocket);
        }
        return datagramSocket;
    }

    public void transactionFailure() {

    }

    public void transactionTimeout(ClientTransaction clientTransaction) {

    }

    public void provResponseReceived(SipResponse sipResponse, Transaction transaction) {

    }

    public void errResponseReceived(SipResponse sipResponse) {

    }

    public void successResponseReceived(SipResponse sipResponse, Transaction transaction) {

    }

    public void transactionTransportError() {

    }
}
