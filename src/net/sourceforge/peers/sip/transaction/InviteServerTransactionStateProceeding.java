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
    
    Copyright 2007 Yohann Martineau 
*/

package net.sourceforge.peers.sip.transaction;

import net.sourceforge.peers.sip.RFC3261;

public class InviteServerTransactionStateProceeding extends
        InviteServerTransactionState {

    public InviteServerTransactionStateProceeding(String id,
            InviteServerTransaction inviteServerTransaction) {
        super(id, inviteServerTransaction);
    }

    @Override
    public void received101To199() {
        InviteServerTransactionState nextState = inviteServerTransaction.PROCEEDING;
        inviteServerTransaction.setState(nextState);
        log(nextState);
    }
    
    @Override
    public void transportError() {
        InviteServerTransactionState nextState = inviteServerTransaction.TERMINATED;
        inviteServerTransaction.setState(nextState);
        log(nextState);
    }
    
    @Override
    public void received2xx() {
        InviteServerTransactionState nextState = inviteServerTransaction.TERMINATED;
        inviteServerTransaction.setState(nextState);
        log(nextState);
        inviteServerTransaction.sendLastResponse();
    }
    
    @Override
    public void received300To699() {
        InviteServerTransactionState nextState = inviteServerTransaction.COMPLETED;
        inviteServerTransaction.setState(nextState);
        log(nextState);
        inviteServerTransaction.sendLastResponse();
        if (RFC3261.TRANSPORT_UDP.equals(inviteServerTransaction.transport)) {
            inviteServerTransaction.timer.schedule(
                    inviteServerTransaction.new TimerG(), RFC3261.TIMER_T1);
        }
        inviteServerTransaction.timer.schedule(
                inviteServerTransaction.new TimerH(), 64 * RFC3261.TIMER_T1);
    }
    
    @Override
    public void receivedInvite() {
        InviteServerTransactionState nextState = inviteServerTransaction.PROCEEDING;
        inviteServerTransaction.setState(nextState);
        log(nextState);
    }
    
    
}
