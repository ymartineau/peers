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

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

public class CallFrame implements ActionListener, WindowListener {

    public static final String HANGUP_ACTION_COMMAND    = "hangup";
    public static final String PICKUP_ACTION_COMMAND    = "pickup";
    public static final String BUSY_HERE_ACTION_COMMAND = "busyhere";
    public static final String CLOSE_ACTION_COMMAND     = "close";

    private CallFrameState state;

    public final CallFrameState INIT;
    public final CallFrameState UAC;
    public final CallFrameState UAS;
    public final CallFrameState RINGING;
    public final CallFrameState SUCCESS;
    public final CallFrameState FAILED;
    public final CallFrameState REMOTE_HANGUP;
    public final CallFrameState TERMINATED;

    private JFrame frame;
    private JPanel callPanel;
    private JPanel callPanelContainer;
    private CallFrameListener callFrameListener;
    private SipRequest sipRequest;

    CallFrame(String remoteParty, String id,
            CallFrameListener callFrameListener, Logger logger) {
        INIT = new CallFrameStateInit(id, this, logger);
        UAC = new CallFrameStateUac(id, this, logger);
        UAS = new CallFrameStateUas(id, this, logger);
        RINGING = new CallFrameStateRinging(id, this, logger);
        SUCCESS = new CallFrameStateSuccess(id, this, logger);
        FAILED = new CallFrameStateFailed(id, this, logger);
        REMOTE_HANGUP = new CallFrameStateRemoteHangup(id, this, logger);
        TERMINATED = new CallFrameStateTerminated(id, this, logger);
        state = INIT;
        this.callFrameListener = callFrameListener;
        frame = new JFrame(remoteParty);
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        JLabel remotePartyLabel = new JLabel(remoteParty);
        Border remotePartyBorder = BorderFactory.createEmptyBorder(5, 5, 0, 5);
        remotePartyLabel.setBorder(remotePartyBorder);
        remotePartyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPane.add(remotePartyLabel);
        Keypad keypad = new Keypad(this);
        contentPane.add(keypad);
        callPanelContainer = new JPanel();
        contentPane.add(callPanelContainer);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(this);
    }

    public void callClicked() {
        state.callClicked();
    }

    public void incomingCall() {
        state.incomingCall();
    }

    public void remoteHangup() {
        state.remoteHangup();
    }

    public void error(SipResponse sipResponse) {
        state.error(sipResponse);
    }

    public void calleePickup() {
        state.calleePickup();
    }

    public void ringing() {
        state.ringing();
    }

    void hangup() {
        if (callFrameListener != null) {
            callFrameListener.hangupClicked(sipRequest);
        }
    }

    void pickup() {
        if (callFrameListener != null && sipRequest != null) {
            callFrameListener.pickupClicked(sipRequest);
        }
    }

    void busyHere() {
        if (callFrameListener != null && sipRequest != null) {
            frame.dispose();
            callFrameListener.busyHereClicked(sipRequest);
            sipRequest = null;
        }
    }

    void close() {
        frame.dispose();
    }
    
    public void setState(CallFrameState state) {
        this.state.log(state);
        this.state = state;
    }

    public JFrame getFrame() {
        return frame;
    }

    public void setCallPanel(JPanel callPanel) {
        if (this.callPanel != null) {
            callPanelContainer.remove(this.callPanel);
            frame.pack();
        }
        callPanelContainer.add(callPanel);
        frame.pack();
        this.callPanel = callPanel;
    }

    public void addPageEndLabel(String text) {
        Container container = frame.getContentPane();
        JLabel label = new JLabel(text);
        Border labelBorder = BorderFactory.createEmptyBorder(0, 5, 0, 5);
        label.setBorder(labelBorder);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(label);
        frame.pack();
    }

    public void setSipRequest(SipRequest sipRequest) {
        this.sipRequest = sipRequest;
    }

    // action listener methods

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        Runnable runnable = null;
        if (HANGUP_ACTION_COMMAND.equals(actionCommand)) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    state.hangupClicked();
                }
            };
        } else if (CLOSE_ACTION_COMMAND.equals(actionCommand)) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    state.closeClicked();
                }
            };
        } else if (PICKUP_ACTION_COMMAND.equals(actionCommand)) {
            runnable = new Runnable() {
                public void run() {
                    state.pickupClicked();
                }
            };
        } else if (BUSY_HERE_ACTION_COMMAND.equals(actionCommand)) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    state.busyHereClicked();
                }
            };
        }
        if (runnable != null) {
            SwingUtilities.invokeLater(runnable);
        }
    }

    // window listener methods

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
        state.hangupClicked();
    }

    @Override
    public void windowClosing(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    public void keypadEvent(char c) {
        callFrameListener.dtmf(c);
    }

}
