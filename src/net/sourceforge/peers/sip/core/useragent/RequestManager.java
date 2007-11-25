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

package net.sourceforge.peers.sip.core.useragent;

import net.sourceforge.peers.sip.core.useragent.handlers.AckHandler;
import net.sourceforge.peers.sip.core.useragent.handlers.ByeHandler;
import net.sourceforge.peers.sip.core.useragent.handlers.CancelHandler;
import net.sourceforge.peers.sip.core.useragent.handlers.InviteHandler;
import net.sourceforge.peers.sip.core.useragent.handlers.OptionsHandler;
import net.sourceforge.peers.sip.core.useragent.handlers.RegisterHandler;


public abstract class RequestManager {
    protected InviteHandler inviteHandler;
    protected CancelHandler cancelHandler;
    protected AckHandler ackHandler;
    protected ByeHandler byeHandler;
    protected OptionsHandler optionsHandler;
    protected RegisterHandler registerHandler;
    
    public RequestManager() {
        inviteHandler = new InviteHandler();
        cancelHandler = new CancelHandler();
        ackHandler = new AckHandler();
        byeHandler = new ByeHandler();
        optionsHandler = new OptionsHandler();
        registerHandler = new RegisterHandler();
    }

    public InviteHandler getInviteHandler() {
        return inviteHandler;
    }

    public ByeHandler getByeHandler() {
        return byeHandler;
    }
    
}
