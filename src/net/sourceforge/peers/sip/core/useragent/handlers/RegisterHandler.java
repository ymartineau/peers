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

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.core.useragent.InitialRequestManager;
import net.sourceforge.peers.sip.core.useragent.RequestManager;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.NameAddress;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderParamName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transaction.ClientTransaction;
import net.sourceforge.peers.sip.transaction.ClientTransactionUser;
import net.sourceforge.peers.sip.transaction.NonInviteClientTransaction;
import net.sourceforge.peers.sip.transaction.Transaction;
import net.sourceforge.peers.sip.transaction.TransactionManager;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;
import net.sourceforge.peers.sip.transport.TransportManager;

public class RegisterHandler extends MethodHandler
        implements ClientTransactionUser {

    public static final int REFRESH_MARGIN = 10; // seconds

    private InitialRequestManager initialRequestManager;

    private Timer timer;

    private String requestUriStr;
    private String profileUriStr;
    private String callIDStr;
    
    //FIXME should be on a profile based context
    private boolean unregistered;
    
    public RegisterHandler(UserAgent userAgent,
            TransactionManager transactionManager,
            TransportManager transportManager) {
        super(userAgent, transactionManager, transportManager);
        timer = new Timer();
        unregistered = false;
    }

    //TODO factorize common code here and in invitehandler
    public ClientTransaction preProcessRegister(SipRequest sipRequest) {
        SipHeaders sipHeaders = sipRequest.getSipHeaders();
        SipURI destinationUri = RequestManager.getDestinationUri(sipRequest);
        int port = destinationUri.getPort();
        if (port == SipURI.DEFAULT_PORT) {
            port = RFC3261.TRANSPORT_DEFAULT_PORT;
        }
        //TODO if header route is present, addrspec = toproute.nameaddress.addrspec
        String transport = RFC3261.TRANSPORT_UDP;
        Hashtable<String, String> params = destinationUri.getUriParameters();
        if (params != null) {
            String reqUriTransport = params.get(RFC3261.PARAM_TRANSPORT);
            if (reqUriTransport != null) {
                transport = reqUriTransport; 
            }
        }
        ClientTransaction clientTransaction = transactionManager
            .createClientTransaction(sipRequest, destinationUri.getHost(), port,
                    transport, null, this);
        //TODO 10.2
        SipHeaderFieldValue to = sipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_TO));
        SipHeaderFieldValue from = sipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_FROM));
        String fromValue = from.getValue();
        to.setValue(fromValue);
        requestUriStr = destinationUri.toString();
        profileUriStr = NameAddress.nameAddressToUri(fromValue);
        callIDStr = sipHeaders.get(new SipHeaderFieldName(RFC3261.HDR_CALLID))
            .toString();
        return clientTransaction;
    }

    public void unregister() {
        timer.cancel();
        unregistered = true;
        challenged = false;
    }

    //////////////////////////////////////////////////////////
    // ClientTransactionUser methods
    //////////////////////////////////////////////////////////
    
    public void errResponseReceived(SipResponse sipResponse) {
        int statusCode = sipResponse.getStatusCode();
        if ((statusCode == RFC3261.CODE_401_UNAUTHORIZED
                || statusCode == RFC3261.CODE_407_PROXY_AUTHENTICATION_REQUIRED)
                && challengeManager != null) {
            if (!challenged) {
                NonInviteClientTransaction nonInviteClientTransaction =
                    (NonInviteClientTransaction)
                    transactionManager.getClientTransaction(sipResponse);
                SipRequest sipRequest = nonInviteClientTransaction.getRequest();
                challengeManager.handleChallenge(sipRequest, sipResponse);
                challenged = true;
            } else {
                SipListener sipListener = userAgent.getSipListener();
                if (sipListener != null) {
                    sipListener.registerFailed(sipResponse);
                }
            }
        }
    }

    public void provResponseReceived(SipResponse sipResponse,
            Transaction transaction) {
        //meaningless
    }

    public void successResponseReceived(SipResponse sipResponse,
            Transaction transaction) {
        // 1. retrieve request corresponding to response
        // 2. if request was register, extract contact and expires, and start
        //    register refresh timer
        // 3. notify sip listener of register success event.
        SipRequest sipRequest = transaction.getRequest();
        SipHeaderFieldName contactName = new SipHeaderFieldName(
                RFC3261.HDR_CONTACT);
        SipHeaderFieldValue registerContact = sipRequest.getSipHeaders()
                .get(contactName);
        SipHeaderParamName expiresParam = new SipHeaderParamName(
                RFC3261.PARAM_EXPIRES);
        String expires = registerContact.getParam(expiresParam);
        if (expires == null || "".equals(expires.trim())) {
            return;
        }
        challenged = false;
        if (!"0".equals(expires)) {
            // each contact contains an expires parameter giving the expiration
            // in seconds. Thus the binding must be refreshed before it expires.
            SipHeaders sipHeaders = sipResponse.getSipHeaders();
            SipHeaderFieldValue contact = sipHeaders.get(contactName);
            if (contact == null) {
                return;
            }
            expires = contact.getParam(expiresParam);
            if (expires == null || "".equals(expires.trim())) {
                return;
            }
            if (!unregistered) {
                int delay = Integer.parseInt(expires) - REFRESH_MARGIN;
                timer.schedule(new RefreshTimerTask(), delay * 1000);
            }
        }
        SipListener sipListener = userAgent.getSipListener();
        if (sipListener != null) {
            sipListener.registerSuccessful(sipResponse);
        }
    }

    public void transactionTimeout(ClientTransaction clientTransaction) {
        //TODO alert user
    }

    public void transactionTransportError() {
        //TODO alert user
    }

    //////////////////////////////////////////////////////////
    // TimerTask
    //////////////////////////////////////////////////////////
    
    class RefreshTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                initialRequestManager.createInitialRequest(requestUriStr,
                        RFC3261.METHOD_REGISTER, profileUriStr, callIDStr);
            } catch (SipUriSyntaxException e) {
                Logger.error("syntax error", e);
            }
        }
    }

    public void setInitialRequestManager(InitialRequestManager initialRequestManager) {
        this.initialRequestManager = initialRequestManager;
    }

}
