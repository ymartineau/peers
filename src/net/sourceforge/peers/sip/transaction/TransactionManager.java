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

package net.sourceforge.peers.sip.transaction;

import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Timer;

import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderParamName;
import net.sourceforge.peers.sip.transport.SipMessage;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;
import net.sourceforge.peers.sip.transport.TransportManager;


public class TransactionManager {

    protected Timer timer;
    
    private Hashtable<String, ClientTransaction> clientTransactions;
    private Hashtable<String, ServerTransaction> serverTransactions;

    private TransportManager transportManager;
    
    public TransactionManager() {
        clientTransactions = new Hashtable<String, ClientTransaction>();
        serverTransactions = new Hashtable<String, ServerTransaction>();
        timer = new Timer("Transaction timer");
    }
    
    //TODO remove transportManager parameter, use class variable transportManager
    public ClientTransaction createClientTransaction(SipRequest sipRequest,
            InetAddress inetAddress, int port, String transport, String pBranchId,
            ClientTransactionUser clientTransactionUser) {
        String branchId;
        if (pBranchId == null || "".equals(pBranchId.trim())
                || !pBranchId.startsWith(RFC3261.BRANCHID_MAGIC_COOKIE)) {
            branchId = Utils.generateBranchId();
        } else {
            branchId = pBranchId;
        }
        String method = sipRequest.getMethod();
        ClientTransaction clientTransaction;
        if (RFC3261.METHOD_INVITE.equals(method)) {
            clientTransaction = new InviteClientTransaction(branchId,
                    inetAddress, port, transport, sipRequest, clientTransactionUser,
                    timer, transportManager);
        } else {
            clientTransaction = new NonInviteClientTransaction(branchId,
                    inetAddress, port, transport, sipRequest, clientTransactionUser,
                    timer, transportManager);
        }
        clientTransactions.put(getTransactionId(branchId, method),
                clientTransaction);
        return clientTransaction;
    }

    public ServerTransaction createServerTransaction(SipResponse sipResponse,
            int port, String transport,
            ServerTransactionUser serverTransactionUser,
            SipRequest sipRequest) {
        SipHeaderFieldValue via = Utils.getTopVia(sipResponse);
        String branchId = via.getParam(new SipHeaderParamName(
                RFC3261.PARAM_BRANCH));
        String cseq = sipResponse.getSipHeaders().get(
                new SipHeaderFieldName(RFC3261.HDR_CSEQ)).toString();
        String method = cseq.substring(cseq.lastIndexOf(' ') + 1);
        ServerTransaction serverTransaction;
        // TODO create server transport user and pass it to server transaction
        if (RFC3261.METHOD_INVITE.equals(method)) {
            serverTransaction = new InviteServerTransaction(branchId, port,
                    transport, sipResponse, serverTransactionUser, sipRequest,
                    timer, this, transportManager);
            // serverTransaction = new InviteServerTransaction(branchId);
        } else {
            serverTransaction = new NonInviteServerTransaction(branchId, port,
                    transport, method, serverTransactionUser, sipRequest, timer,
                    transportManager);
        }
        serverTransactions.put(getTransactionId(branchId, method),
                serverTransaction);
        return serverTransaction;
    }

    public ClientTransaction getClientTransaction(SipMessage sipMessage) {
        SipHeaderFieldValue via = Utils.getTopVia(sipMessage);
        String branchId = via.getParam(new SipHeaderParamName(
                RFC3261.PARAM_BRANCH));
        String cseq = sipMessage.getSipHeaders().get(
                new SipHeaderFieldName(RFC3261.HDR_CSEQ)).toString();
        String method = cseq.substring(cseq.lastIndexOf(' ') + 1);
        return clientTransactions.get(getTransactionId(branchId, method));
    }

    public ServerTransaction getServerTransaction(SipMessage sipMessage) {
        SipHeaderFieldValue via = Utils.getTopVia(sipMessage);
        String branchId = via.getParam(new SipHeaderParamName(
                RFC3261.PARAM_BRANCH));
        String method;
        if (sipMessage instanceof SipRequest) {
            method = ((SipRequest)sipMessage).getMethod();
        } else {
            String cseq = sipMessage.getSipHeaders().get(
                    new SipHeaderFieldName(RFC3261.HDR_CSEQ)).toString();
            method = cseq.substring(cseq.lastIndexOf(' ') + 1);
        }
        if (RFC3261.METHOD_ACK.equals(method)) {
            method = RFC3261.METHOD_INVITE;
            // TODO if positive response, ACK does not belong to transaction
            // retrieve transaction and take responses from transaction
            // and check if a positive response has been received
            // if it is the case, a new standalone transaction must be created
            // for the ACK
        }
        return serverTransactions.get(getTransactionId(branchId, method));
    }

    public ServerTransaction getServerTransaction(String branchId, String method) {
        return serverTransactions.get(getTransactionId(branchId, method));
    }
    
    void removeServerTransaction(String branchId, String method) {
        serverTransactions.remove(getTransactionId(branchId, method));
    }
    
    private String getTransactionId(String branchId, String method) {
        StringBuffer buf = new StringBuffer();
        buf.append(branchId);
        buf.append(Transaction.ID_SEPARATOR);
        buf.append(method);
        return buf.toString();
    }

    public void setTransportManager(TransportManager transportManager) {
        this.transportManager = transportManager;
    }

}
