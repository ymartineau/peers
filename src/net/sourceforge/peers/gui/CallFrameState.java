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
    
    Copyright 2010 Yohann Martineau 
*/

package net.sourceforge.peers.gui;

import javax.swing.JPanel;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.AbstractState;
import net.sourceforge.peers.sip.transport.SipResponse;

public abstract class CallFrameState extends AbstractState {

    protected CallFrame callFrame;
    protected JPanel callPanel;

    public CallFrameState(String id, CallFrame callFrame, Logger logger) {
        super(id, logger);
        this.callFrame = callFrame;
    }

    public void callClicked() {}
    public void incomingCall() {}
    public void calleePickup() {}
    public void error(SipResponse sipResponse) {}
    public void pickupClicked() {}
    public void busyHereClicked() {}
    public void hangupClicked() {}
    public void remoteHangup() {}
    public void closeClicked() {}
    public void ringing() {}
    
}
