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

package net.sourceforge.peers.sip.transaction;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;
import net.sourceforge.peers.sip.transport.TransportManager;


public class NonInviteServerTransaction extends Transaction
        implements ServerTransaction/*, SipServerTransportUser*/ {

    public final NonInviteServerTransactionState TRYING;
    public final NonInviteServerTransactionState PROCEEDING;
    public final NonInviteServerTransactionState COMPLETED;
    public final NonInviteServerTransactionState TERMINATED;
    
    protected ServerTransactionUser serverTransactionUser;
    protected Timer timer;
    protected String transport;
    
    private NonInviteServerTransactionState state;
    //private int port;
    
    NonInviteServerTransaction(String branchId, int port, String transport,
            String method, ServerTransactionUser serverTransactionUser,
            SipRequest sipRequest) {
        super(branchId, method);
        
        TRYING = new NonInviteServerTransactionStateTrying(getId(), this);
        PROCEEDING = new NonInviteServerTransactionStateProceeding(getId(), this);
        COMPLETED = new NonInviteServerTransactionStateCompleted(getId(), this);
        TERMINATED = new NonInviteServerTransactionStateTerminated(getId(), this);
        
        state = TRYING;
        
        //this.port = port;
        this.transport = transport;
        timer = TransactionManager.getInstance().timer;
        this.serverTransactionUser = serverTransactionUser;
        request = sipRequest;
//        sipServerTransport = SipTransportFactory.getInstance()
//            .createServerTransport(this, port, transport);
        try {
            TransportManager.getInstance().createServerTransport(transport, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        //TODO pass request to TU
    }

    public void setState(NonInviteServerTransactionState state) {
        this.state = state;
    }
    
    public void receivedRequest(SipRequest sipRequest) {
        state.receivedRequest();
    }

    public void sendReponse(SipResponse sipResponse) {
        responses.add(sipResponse);
        int statusCode = sipResponse.getStatusCode();
        if (statusCode < RFC3261.CODE_200_OK) {
            state.received1xx();
        } else if (statusCode <= RFC3261.CODE_MAX) {
            state.received200To699();
        }
    }
    
    void sendLastResponse() {
        //sipServerTransport.sendResponse(responses.get(responses.size() - 1));
        int nbOfResponses = responses.size();
        if (nbOfResponses > 0) {
            try {
                TransportManager.getInstance().sendResponse(
                        responses.get(nbOfResponses - 1));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void start() {
        // TODO Auto-generated method stub
        
    }

//    public void messageReceived(SipMessage sipMessage) {
//        // TODO Auto-generated method stub
//        
//    }

    class TimerJ extends TimerTask {
        @Override
        public void run() {
            state.timerJFires();
        }
    }
    
}
