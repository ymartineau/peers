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

public class NonInviteServerTransactionStateProceeding extends
        NonInviteServerTransactionState {

    public NonInviteServerTransactionStateProceeding(String id,
            NonInviteServerTransaction nonInviteServerTransaction) {
        super(id, nonInviteServerTransaction);
    }

    @Override
    public void received1xx() {
        NonInviteServerTransactionState nextState = nonInviteServerTransaction.PROCEEDING;
        nonInviteServerTransaction.setState(nextState);
        log(nextState);
        nonInviteServerTransaction.sendLastResponse();
    }
    
    @Override
    public void received200To699() {
        NonInviteServerTransactionState nextState = nonInviteServerTransaction.COMPLETED;
        nonInviteServerTransaction.setState(nextState);
        log(nextState);
        nonInviteServerTransaction.sendLastResponse();
        int timeout;
        if (RFC3261.TRANSPORT_UDP.equals(nonInviteServerTransaction.transport)) {
            timeout = 64 * RFC3261.TIMER_T1;
        } else {
            timeout = 0;
        }
        nonInviteServerTransaction.timer.schedule(
                nonInviteServerTransaction.new TimerJ(), timeout);
    }
    
    @Override
    public void transportError() {
        NonInviteServerTransactionState nextState = nonInviteServerTransaction.TERMINATED;
        nonInviteServerTransaction.setState(nextState);
        log(nextState);
    }
    
    @Override
    public void receivedRequest() {
        NonInviteServerTransactionState nextState = nonInviteServerTransaction.PROCEEDING;
        nonInviteServerTransaction.setState(nextState);
        log(nextState);
    }
    
}
