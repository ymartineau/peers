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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import net.sourceforge.peers.Config;
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
    private boolean unregisterInvoked;
    private boolean registered;
    private boolean triedWithReceived;
    
    public RegisterHandler(UserAgent userAgent,
            TransactionManager transactionManager,
            TransportManager transportManager) {
        super(userAgent, transactionManager, transportManager);
    }

    //TODO factorize common code here and in invitehandler
    public synchronized ClientTransaction preProcessRegister(SipRequest sipRequest)
            throws SipUriSyntaxException {
        registered = false;
        unregisterInvoked = false;
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
        SipURI sipUri = userAgent.getConfig().getOutboundProxy();
        if (sipUri == null) {
            sipUri = destinationUri;
        }
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(sipUri.getHost());
        } catch (UnknownHostException e) {
            throw new SipUriSyntaxException("unknown host: "
                    + sipUri.getHost(), e);
        }
        ClientTransaction clientTransaction = transactionManager
            .createClientTransaction(sipRequest, inetAddress, port,
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
        unregisterInvoked = true;
        challenged = false;
    }

    //////////////////////////////////////////////////////////
    // ClientTransactionUser methods
    //////////////////////////////////////////////////////////
    
    public void errResponseReceived(SipResponse sipResponse) {
        int statusCode = sipResponse.getStatusCode();
        if ((statusCode == RFC3261.CODE_401_UNAUTHORIZED
                || statusCode == RFC3261.CODE_407_PROXY_AUTHENTICATION_REQUIRED)
            && challengeManager != null & !challenged) {
            NonInviteClientTransaction nonInviteClientTransaction =
                (NonInviteClientTransaction)
                transactionManager.getClientTransaction(sipResponse);
            SipRequest sipRequest = nonInviteClientTransaction.getRequest();
            challengeManager.handleChallenge(sipRequest, sipResponse);
            challenged = true;
        } else {
            challenged = false;
            boolean notifyListener = true;
            if (!triedWithReceived) {
                triedWithReceived = true;
                userAgent.closeTransports();
                SipHeaders sipHeaders = sipResponse.getSipHeaders();
                SipHeaderFieldName viaName = new SipHeaderFieldName(
                        RFC3261.HDR_VIA);
                SipHeaderFieldValue via = sipHeaders.get(viaName);
                SipHeaderParamName receivedName = new SipHeaderParamName(
                        RFC3261.PARAM_RECEIVED);
                String received = via.getParam(receivedName);
                InetAddress inetAddress;
                try {
                    inetAddress = InetAddress.getByName(received);
                    if (received != null) {
                        Config config = userAgent.getConfig();
                        config.setInetAddress(inetAddress);
                        try {
                            userAgent.getUac().register();
                            notifyListener = false;
                        } catch (SipUriSyntaxException e) {
                            Logger.error(e.getMessage(), e);
                        }
                    }
                } catch (UnknownHostException e) {
                    Logger.error("unknown host: " + received, e);
                }
            }
            triedWithReceived = false;
            if (notifyListener) {
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

    public synchronized void successResponseReceived(SipResponse sipResponse,
            Transaction transaction) {
        // 1. retrieve request corresponding to response
        // 2. if request was not an unregister, extract contact and expires,
        //    and start register refresh timer
        // 3. notify sip listener of register success event.
        SipRequest sipRequest = transaction.getRequest();
        SipHeaderFieldName contactName = new SipHeaderFieldName(
                RFC3261.HDR_CONTACT);
        SipHeaderFieldValue requestContact = sipRequest.getSipHeaders()
                .get(contactName);
        SipHeaderParamName expiresParam = new SipHeaderParamName(
                RFC3261.PARAM_EXPIRES);
        String expires = requestContact.getParam(expiresParam);
        challenged = false;
        if (!"0".equals(expires)) {
            // each contact contains an expires parameter giving the expiration
            // in seconds. Thus the binding must be refreshed before it expires.
            SipHeaders sipHeaders = sipResponse.getSipHeaders();
            SipHeaderFieldValue responseContact = sipHeaders.get(contactName);
            if (responseContact == null) {
                return;
            }
            expires = responseContact.getParam(expiresParam);
            if (expires == null || "".equals(expires.trim())) {
                return;
            }
            registered = true;
            if (!unregisterInvoked) {
                int delay = Integer.parseInt(expires) - REFRESH_MARGIN;
                timer = new Timer();
                timer.schedule(new RefreshTimerTask(), delay * 1000);
            }
        }
        SipListener sipListener = userAgent.getSipListener();
        if (sipListener != null) {
            sipListener.registerSuccessful(sipResponse);
        }
    }

    public void transactionTimeout(ClientTransaction clientTransaction) {
        SipListener sipListener = userAgent.getSipListener();
        if (sipListener != null) {
            sipListener.registerFailed(null);
        }
    }

    public void transactionTransportError() {
        //TODO alert user
    }

    public boolean isRegistered() {
        return registered;
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
