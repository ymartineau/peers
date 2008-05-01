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

package net.sourceforge.peers.sip.core.useragent;

import net.sourceforge.peers.sip.core.useragent.handlers.AckHandler;
import net.sourceforge.peers.sip.core.useragent.handlers.ByeHandler;
import net.sourceforge.peers.sip.core.useragent.handlers.CancelHandler;
import net.sourceforge.peers.sip.core.useragent.handlers.InviteHandler;
import net.sourceforge.peers.sip.core.useragent.handlers.OptionsHandler;
import net.sourceforge.peers.sip.core.useragent.handlers.RegisterHandler;
import net.sourceforge.peers.sip.transaction.TransactionManager;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.TransportManager;


public abstract class RequestManager {
    protected InviteHandler inviteHandler;
    protected CancelHandler cancelHandler;
    protected AckHandler ackHandler;
    protected ByeHandler byeHandler;
    protected OptionsHandler optionsHandler;
    protected RegisterHandler registerHandler;
    
    protected UserAgent userAgent;
    protected TransactionManager transactionManager;
    protected TransportManager transportManager;
    
    public RequestManager(UserAgent userAgent,
            DialogManager dialogManager,
            TransactionManager transactionManager,
            TransportManager transportManager) {
        inviteHandler = new InviteHandler(userAgent, dialogManager,
                transactionManager, transportManager);
        cancelHandler = new CancelHandler(userAgent, dialogManager,
                transactionManager, transportManager);
        ackHandler = new AckHandler(transactionManager,
                transportManager);
        byeHandler = new ByeHandler(userAgent, dialogManager,
                transactionManager, transportManager);
        optionsHandler = new OptionsHandler(transactionManager,
                transportManager);
        registerHandler = new RegisterHandler(transactionManager,
                transportManager);
        this.userAgent = userAgent;
        this.transactionManager = transactionManager;
        this.transportManager = transportManager;
    }

    public InviteHandler getInviteHandler() {
        return inviteHandler;
    }

    public ByeHandler getByeHandler() {
        return byeHandler;
    }
    
}
