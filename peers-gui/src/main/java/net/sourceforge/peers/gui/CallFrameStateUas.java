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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sourceforge.peers.Logger;

public class CallFrameStateUas extends CallFrameState {

    public CallFrameStateUas(String id, CallFrame callFrame, Logger logger) {
        super(id, callFrame, logger);
        callPanel = new JPanel();
        callPanel.add(new JLabel("Incoming call"));
        JButton hangupButton = new JButton("Busy here");
        hangupButton.setActionCommand(CallFrame.BUSY_HERE_ACTION_COMMAND);
        hangupButton.addActionListener(callFrame);
        callPanel.add(hangupButton);
        JButton pickupButton = new JButton("Pickup");
        pickupButton.setActionCommand(CallFrame.PICKUP_ACTION_COMMAND);
        pickupButton.addActionListener(callFrame);
        callPanel.add(pickupButton);
    }

    @Override
    public void pickupClicked() {
        callFrame.setState(callFrame.SUCCESS);
        callFrame.pickup();
        callFrame.setCallPanel(callFrame.SUCCESS.callPanel);
    }

    @Override
    public void busyHereClicked() {
        callFrame.setState(callFrame.TERMINATED);
        callFrame.busyHere();
    }

    @Override
    public void remoteHangup() {
        callFrame.setState(callFrame.REMOTE_HANGUP);
        callFrame.setCallPanel(callFrame.REMOTE_HANGUP.callPanel);
    }

}
