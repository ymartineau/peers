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

import net.sourceforge.peers.sip.RFC3261;

public class InviteServerTransactionStateCompleted extends
        InviteServerTransactionState {

    public InviteServerTransactionStateCompleted(String id,
            InviteServerTransaction inviteServerTransaction) {
        super(id, inviteServerTransaction);
    }

    @Override
    public void timerGFires() {
        InviteServerTransactionState nextState = inviteServerTransaction.COMPLETED;
        inviteServerTransaction.setState(nextState);
        log(nextState);
        inviteServerTransaction.sendLastResponse();
        long delay = (long)Math.pow(2,
                ++inviteServerTransaction.nbRetrans) * RFC3261.TIMER_T1;
        inviteServerTransaction.timer.schedule(
                inviteServerTransaction.new TimerG(),
                Math.min(delay, RFC3261.TIMER_T2));
    }
    
    @Override
    public void timerHFiresOrTransportError() {
        InviteServerTransactionState nextState = inviteServerTransaction.TERMINATED;
        inviteServerTransaction.setState(nextState);
        log(nextState);
        inviteServerTransaction.serverTransactionUser.transactionFailure();
    }
    
    @Override
    public void receivedAck() {
        InviteServerTransactionState nextState = inviteServerTransaction.CONFIRMED;
        inviteServerTransaction.setState(nextState);
        log(nextState);
        int delay;
        if (RFC3261.TRANSPORT_UDP.equals(inviteServerTransaction.transport)) {
            delay = RFC3261.TIMER_T4;
        } else {
            delay = 0;
        }
        inviteServerTransaction.timer.schedule(
                inviteServerTransaction.new TimerI(), delay);
    }
    
    @Override
    public void receivedInvite() {
        InviteServerTransactionState nextState = inviteServerTransaction.COMPLETED;
        inviteServerTransaction.setState(nextState);
        log(nextState);
        // retransmission
        inviteServerTransaction.sendLastResponse();
    }
    
}
