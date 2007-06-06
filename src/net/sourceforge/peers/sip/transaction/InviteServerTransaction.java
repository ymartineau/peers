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
import net.sourceforge.peers.sip.transport.SipMessage;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;
import net.sourceforge.peers.sip.transport.SipServerTransportUser;
import net.sourceforge.peers.sip.transport.TransportManager;


public class InviteServerTransaction extends InviteTransaction
        implements ServerTransaction, SipServerTransportUser {

    public final InviteServerTransactionState INIT;
    public final InviteServerTransactionState PROCEEDING;
    public final InviteServerTransactionState COMPLETED;
    public final InviteServerTransactionState CONFIRMED;
    public final InviteServerTransactionState TERMINATED;
    
    protected String transport;
    protected Timer timer;
    protected int nbRetrans;
    protected ServerTransactionUser serverTransactionUser;
    
    private InviteServerTransactionState state;
    //private SipServerTransport sipServerTransport;
    private int port;
    
    InviteServerTransaction(String branchId, int port, String transport,
            SipResponse sipResponse, ServerTransactionUser serverTransactionUser) {
        super(branchId);
        
        INIT = new InviteServerTransactionStateInit(getId(), this);
        PROCEEDING = new InviteServerTransactionStateProceeding(getId(), this);
        COMPLETED = new InviteServerTransactionStateCompleted(getId(), this);
        CONFIRMED = new InviteServerTransactionStateConfirmed(getId(), this);
        TERMINATED = new InviteServerTransactionStateTerminated(getId(), this);
        
        state = INIT;
        
        this.port = port;
        this.transport = transport;
        responses.add(sipResponse);
        timer = TransactionManager.getInstance().timer;
        nbRetrans = 0;
        this.serverTransactionUser = serverTransactionUser;
        //TODO pass INV to TU, send 100 if TU won't in 200ms
    }

    public void start() {
        state.start();
        
//        sipServerTransport = SipTransportFactory.getInstance()
//            .createServerTransport(this, port, transport);
        try {
            TransportManager.getInstance().createServerTransport(transport, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void receivedRequest(SipRequest sipRequest) {
        state.receivedInvite();
    }

    public void sendReponse(SipResponse sipResponse) {
        //TODO check that a retransmission response will be considered as
        //equal (for contains) to the first response
        if (!responses.contains(sipResponse)) {
            responses.add(sipResponse);
        }
        int statusCode = sipResponse.getStatusCode();
        if (statusCode == RFC3261.CODE_MIN_PROV) {
            // TODO 100 trying
        } else if (statusCode < RFC3261.CODE_MIN_SUCCESS) {
            state.received101To199();
        } else if (statusCode < RFC3261.CODE_MIN_REDIR) {
            state.received2xx();
        } else if (statusCode <= RFC3261.CODE_MAX) {
            state.received300To699();
        } else {
            System.err.println("invalid response code");
        }
    }

    public void setState(InviteServerTransactionState state) {
        this.state = state;
    }

    public void messageReceived(SipMessage sipMessage) {
        // TODO Auto-generated method stub
        
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
    
//    void stopSipServerTransport() {
//        sipServerTransport.stop();
//    }
    
    class TimerG extends TimerTask {
        @Override
        public void run() {
            state.timerGFires();
        }
    }
    
    class TimerH extends TimerTask {
        @Override
        public void run() {
            state.timerHFiresOrTransportError();
        }
    }
    
    class TimerI extends TimerTask {
        @Override
        public void run() {
            state.timerIFires();
        }
    }
    
}
