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

package net.sourceforge.peers.sip.transaction;

import net.sourceforge.peers.Logger;

public class InviteClientTransactionStateTerminated extends
        InviteClientTransactionState {

    public InviteClientTransactionStateTerminated(String id,
            InviteClientTransaction inviteClientTransaction, Logger logger) {
        super(id, inviteClientTransaction, logger);
    }

    @Override
    public void received2xx() {
        // when receive 2xx, reply ACK to server, In case some server think this call not succeed, and terminate this call
        logger.info("received 2xx on terminated.");
        inviteClientTransaction.createAndSendAck();
    }
}
