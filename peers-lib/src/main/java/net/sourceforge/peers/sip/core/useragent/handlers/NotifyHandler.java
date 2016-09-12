package net.sourceforge.peers.sip.core.useragent.handlers;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sdp.SDPManager;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.core.useragent.RequestManager;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.transaction.ServerTransaction;
import net.sourceforge.peers.sip.transaction.ServerTransactionUser;
import net.sourceforge.peers.sip.transaction.Transaction;
import net.sourceforge.peers.sip.transaction.TransactionManager;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;
import net.sourceforge.peers.sip.transport.TransportManager;

public class NotifyHandler extends DialogMethodHandler implements ServerTransactionUser{

    public NotifyHandler(UserAgent userAgent, DialogManager dialogManager, TransactionManager transactionManager, TransportManager transportManager, Logger logger) {
        super(userAgent, dialogManager, transactionManager, transportManager, logger);
    }

    public void handleNotify(SipRequest sipRequest) {
        sendSuccessfulResponse(sipRequest, dialogManager.getDialog(sipRequest));
    }

    private synchronized void sendSuccessfulResponse(SipRequest sipRequest, Dialog dialog) {
        sdpManager = new SDPManager(userAgent, logger);

        SipResponse sipResponse =
                RequestManager.generateResponse(
                        sipRequest,
                        dialog,
                        RFC3261.CODE_200_OK,
                        RFC3261.REASON_200_OK);

        ServerTransaction serverTransaction = transactionManager
                .getServerTransaction(sipRequest);
        if (serverTransaction == null) {
            serverTransaction = transactionManager.createServerTransaction(sipResponse,
                            userAgent.getSipPort(), RFC3261.TRANSPORT_UDP, this,
                            sipRequest);
        }
        serverTransaction.start();

        serverTransaction.receivedRequest(sipRequest);

        serverTransaction.sendReponse(sipResponse);
    }

    public void transactionFailure() {
        // TODO handle transaction failure notify
    }

    public void errResponseReceived(SipResponse sipResponse) {
        // TODO Auto-generated method stub
    }

    public void successResponseReceived(SipResponse sipResponse, Transaction transaction) {
        // TODO Auto-generated method stub
    }
}
