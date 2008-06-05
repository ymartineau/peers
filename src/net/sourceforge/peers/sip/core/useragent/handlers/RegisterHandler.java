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

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.core.useragent.InitialRequestManager;
import net.sourceforge.peers.sip.syntaxencoding.NameAddress;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderParamName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transaction.ClientTransaction;
import net.sourceforge.peers.sip.transaction.ClientTransactionUser;
import net.sourceforge.peers.sip.transaction.Transaction;
import net.sourceforge.peers.sip.transaction.TransactionManager;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;
import net.sourceforge.peers.sip.transport.TransportManager;

public class RegisterHandler extends MethodHandler
        implements ClientTransactionUser {

    //seconds
    public static final int REFRESH_MARGIN = 10;

    private InitialRequestManager initialRequestManager;
    
    private Timer timer;
    
    private String requestUriStr;
    private String profileUriStr;
    private String callIDStr;
    
    public RegisterHandler(TransactionManager transactionManager,
            TransportManager transportManager) {
        super(transactionManager, transportManager);
        timer = new Timer();
    }

    //TODO factorize common code here and in invitehandler
    public ClientTransaction preProcessRegister(SipRequest sipRequest) {
        SipURI requestUri = sipRequest.getRequestUri();
        int port = requestUri.getPort();
        if (port == SipURI.DEFAULT_PORT) {
            port = RFC3261.TRANSPORT_DEFAULT_PORT;
        }
        //TODO if header route is present, addrspec = toproute.nameaddress.addrspec
        String transport = RFC3261.TRANSPORT_UDP;
        Hashtable<String, String> params = requestUri.getUriParameters();
        if (params != null) {
            String reqUriTransport = params.get(RFC3261.PARAM_TRANSPORT);
            if (reqUriTransport != null) {
                transport = reqUriTransport; 
            }
        }
        ClientTransaction clientTransaction = transactionManager
            .createClientTransaction(sipRequest, requestUri.getHost(), port,
                    transport, null, this);
        //TODO 10.2
        SipHeaders sipHeaders = sipRequest.getSipHeaders();
        SipHeaderFieldValue to = sipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_TO));
        SipHeaderFieldValue from = sipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_FROM));
        String fromValue = from.getValue();
        to.setValue(fromValue);
        requestUriStr = requestUri.toString();
        profileUriStr = NameAddress.nameAddressToUri(fromValue);
        callIDStr = sipHeaders.get(new SipHeaderFieldName(RFC3261.HDR_CALLID))
            .toString();
        return clientTransaction;
    }
    
//    public void register() {
//        
//    }
    
    public void unregister() {
        if (requestUriStr == null) {
            return;
        }
        SipRequest sipRequest;
        try {
            sipRequest = initialRequestManager.getGenericRequest(requestUriStr,
                    RFC3261.METHOD_REGISTER, profileUriStr);
        } catch (SipUriSyntaxException e) {
            e.printStackTrace();
            return;
        }
        SipHeaders sipHeaders = sipRequest.getSipHeaders();
        SipHeaderFieldValue callID = sipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_CALLID));
        callID.setValue(callIDStr);
        ClientTransaction clientTransaction = preProcessRegister(sipRequest);
        initialRequestManager.addContact(sipRequest,
                clientTransaction.getContact(), profileUriStr);
        SipHeaderFieldValue contact = sipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_CONTACT));
        contact.addParam(new SipHeaderParamName(RFC3261.PARAM_EXPIRES), "0");
        clientTransaction.start();
    }

    //////////////////////////////////////////////////////////
    // ClientTransactionUser methods
    //////////////////////////////////////////////////////////
    
    public void errResponseReceived(SipResponse sipResponse) {
        // TODO OK, notify user
        
    }

    public void provResponseReceived(SipResponse sipResponse,
            Transaction transaction) {
        //meaningless
    }

    public void successResponseReceived(SipResponse sipResponse,
            Transaction transaction) {
        // each contact contains an expires parameter giving the expiration
        // in seconds. Thus the binding must be refreshed before it expires.
        SipHeaders sipHeaders = sipResponse.getSipHeaders();
        SipHeaderFieldValue contact =
            sipHeaders.get(new SipHeaderFieldName(RFC3261.HDR_CONTACT));
        if (contact == null) {
            return;
        }
        String expires =
            contact.getParam(new SipHeaderParamName(RFC3261.PARAM_EXPIRES));
        if (expires == null || "".equals(expires.trim())) {
            return;
        }
        int delay = Integer.parseInt(expires) - REFRESH_MARGIN;
        timer.schedule(new RefreshTimerTask(), delay * 1000);
    }

    public void transactionTimeout() {
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
                e.printStackTrace();
            }
        }
    }

    public void setInitialRequestManager(InitialRequestManager initialRequestManager) {
        this.initialRequestManager = initialRequestManager;
    }
    
}
