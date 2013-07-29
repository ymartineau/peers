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
    
    Copyright 2007-2013 Yohann Martineau 
*/

package net.sourceforge.peers.sip.core.useragent.handlers;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.core.useragent.RequestManager;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.transaction.ClientTransaction;
import net.sourceforge.peers.sip.transaction.ClientTransactionUser;
import net.sourceforge.peers.sip.transaction.NonInviteClientTransaction;
import net.sourceforge.peers.sip.transaction.ServerTransaction;
import net.sourceforge.peers.sip.transaction.ServerTransactionUser;
import net.sourceforge.peers.sip.transaction.Transaction;
import net.sourceforge.peers.sip.transaction.TransactionManager;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;
import net.sourceforge.peers.sip.transport.TransportManager;

public class ByeHandler extends DialogMethodHandler
        implements ServerTransactionUser, ClientTransactionUser {

    public ByeHandler(UserAgent userAgent, DialogManager dialogManager,
            TransactionManager transactionManager,
            TransportManager transportManager, Logger logger) {
        super(userAgent, dialogManager, transactionManager, transportManager,
                logger);
    }

    ////////////////////////////////////////////////
    // methods for UAC
    ////////////////////////////////////////////////
    
    public void preprocessBye(SipRequest sipRequest, Dialog dialog) {

        // 15.1.1
        
        String addrSpec = sipRequest.getRequestUri().toString();
        userAgent.getPeers().remove(addrSpec);
        challengeManager.postProcess(sipRequest);
    }
    
    

    
    
    
    ////////////////////////////////////////////////
    // methods for UAS
    ////////////////////////////////////////////////
    
    public void handleBye(SipRequest sipRequest, Dialog dialog) {
        dialog.receivedOrSentBye();
        //String remoteUri = dialog.getRemoteUri();

        String addrSpec = sipRequest.getRequestUri().toString();
        userAgent.getPeers().remove(addrSpec);
        dialogManager.removeDialog(dialog.getId());
        logger.debug("removed dialog " + dialog.getId());
        userAgent.getMediaManager().stopSession();
        
        SipResponse sipResponse =
            RequestManager.generateResponse(
                    sipRequest,
                    dialog,
                    RFC3261.CODE_200_OK,
                    RFC3261.REASON_200_OK);
        
        // TODO determine port and transport for server transaction>transport
        // from initial invite
        // FIXME determine port and transport for server transaction>transport
        ServerTransaction serverTransaction = transactionManager
            .createServerTransaction(
                    sipResponse,
                    userAgent.getSipPort(),
                    RFC3261.TRANSPORT_UDP,
                    this,
                    sipRequest);
        
        serverTransaction.start();
        
        serverTransaction.receivedRequest(sipRequest);
        
        serverTransaction.sendReponse(sipResponse);
        
        dialogManager.removeDialog(dialog.getId());

        SipListener sipListener = userAgent.getSipListener();
        if (sipListener != null) {
            sipListener.remoteHangup(sipRequest);
        }

//        setChanged();
//        notifyObservers(sipRequest);
    }

    ///////////////////////////////////////
    //ServerTransactionUser methods
    ///////////////////////////////////////
    public void transactionFailure() {
        // TODO Auto-generated method stub
        
    }

    ///////////////////////////////////////
    //ClientTransactionUser methods
    ///////////////////////////////////////
	@Override
	public void transactionTimeout(ClientTransaction clientTransaction) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void provResponseReceived(SipResponse sipResponse,
			Transaction transaction) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void errResponseReceived(SipResponse sipResponse) {
		int statusCode = sipResponse.getStatusCode();
        if (statusCode == RFC3261.CODE_401_UNAUTHORIZED
                || statusCode == RFC3261.CODE_407_PROXY_AUTHENTICATION_REQUIRED
                && !challenged) {
        	NonInviteClientTransaction nonInviteClientTransaction =
                (NonInviteClientTransaction)
                transactionManager.getClientTransaction(sipResponse);
            SipRequest sipRequest = nonInviteClientTransaction.getRequest();
            String password = userAgent.getConfig().getPassword();
            if (password != null && !"".equals(password.trim())) {
                challengeManager.handleChallenge(sipRequest,
                        sipResponse);
            }
        	challenged = true;
        } else {
        	challenged = false;
        }
	}

	@Override
	public void successResponseReceived(SipResponse sipResponse,
			Transaction transaction) {
		Dialog dialog = dialogManager.getDialog(sipResponse);
		if (dialog == null) {
		    return;
		}
		dialog.receivedOrSentBye();
		dialogManager.removeDialog(dialog.getId());
        logger.debug("removed dialog " + dialog.getId());
	}

	@Override
	public void transactionTransportError() {
		// TODO Auto-generated method stub
		
	}


    
    
}
