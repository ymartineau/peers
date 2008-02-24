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

package net.sourceforge.peers.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.core.useragent.UAC;
import net.sourceforge.peers.sip.core.useragent.UAS;
import net.sourceforge.peers.sip.core.useragent.handlers.InviteHandler;
import net.sourceforge.peers.sip.syntaxencoding.NameAddress;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.SipRequest;

public class CallFrame implements ActionListener, Observer {

    public final static String ACCEPT_ACTION = "Accept";
    public final static String REJECT_ACTION = "Reject";
    public final static String HANGUP_ACTION = "Hangup";
    
    private InviteHandler inviteHandler;
    private SipRequest sipRequest;
    
    private String peer;
    private JFrame frame;
    private JPanel mainPanel;
    private JLabel text;
    private JButton acceptButton;
    private JButton rejectButton;
    private JButton hangupButton;

    //used by uac
    public CallFrame(String peer) {
        commonInit(peer);

        hangupButton = new JButton(HANGUP_ACTION);
        hangupButton.setActionCommand(HANGUP_ACTION);
        hangupButton.addActionListener(this);

        mainPanel.add(text);
        mainPanel.add(hangupButton);
        
        commonPack();
    }
    
    //if used by uas
    public CallFrame(InviteHandler inviteHandler, SipRequest sipRequest) {
        this.inviteHandler = inviteHandler;
        this.sipRequest = sipRequest;
        
        SipHeaderFieldValue from = sipRequest.getSipHeaders().get(
                new SipHeaderFieldName(RFC3261.HDR_FROM));
        
        String remoteUri;
        if (from.toString().indexOf(RFC3261.LEFT_ANGLE_BRACKET) > -1) {
            remoteUri = NameAddress.nameAddressToUri(from.toString());
        } else {
            int separatorPos = from.toString().indexOf(RFC3261.PARAM_SEPARATOR);
            remoteUri = from.toString().substring(0, separatorPos);
        }

        commonInit(remoteUri);
        
        acceptButton = new JButton(ACCEPT_ACTION);
        acceptButton.setActionCommand(ACCEPT_ACTION);
        acceptButton.addActionListener(this);
        
        rejectButton = new JButton(REJECT_ACTION);
        rejectButton.setActionCommand(REJECT_ACTION);
        rejectButton.addActionListener(this);
        

        
        mainPanel.add(text);
        mainPanel.add(acceptButton);
        mainPanel.add(rejectButton);
        
        commonPack();
    }

    private void commonInit(String peer) {
        this.peer = peer;
        frame = new JFrame(peer);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                if (acceptButton == null) {
                    hangup();
                } else {
                    rejectCall();
                }
            }
        });
        mainPanel = new JPanel();
        text = new JLabel("Session with: " + peer);
    }
    
    private void commonPack() {
        frame.getContentPane().add(mainPanel);
        
        frame.pack();
        frame.setVisible(true);
        
        UAS.getInstance().getMidDialogRequestManager().getByeHandler().addObserver(this);
    }
    
    private void acceptCall() {
        
        //SIP core
        Thread thread = new Thread() {
            @Override
            public void run() {
                inviteHandler.acceptCall(sipRequest);
            }
        };
        thread.start();
        
        //GUI
        hangupButton = new JButton(HANGUP_ACTION);
        hangupButton.setActionCommand(HANGUP_ACTION);
        hangupButton.addActionListener(this);
        mainPanel.remove(acceptButton);
        mainPanel.remove(rejectButton);
        acceptButton = null;
        rejectButton = null;
        mainPanel.add(hangupButton);
        frame.pack();
        UAS.getInstance().getMidDialogRequestManager().getByeHandler().addObserver(this);
    }
    
    private void rejectCall() {
        
        //SIP core
        Thread thread = new Thread() {
            @Override
            public void run() {
                inviteHandler.rejectCall(sipRequest);
            }
        };
        thread.start();
        
        //GUI
        closeFrame();
    }
    
    private void hangup() {
        
        //SIP core
        Thread thread = new Thread() {
            @Override
            public void run() {
                Dialog dialog = DialogManager.getInstance().getDialog(peer);
                UAC.getInstance().terminate(dialog);
            }
        };
        thread.start();
        
        //GUI
        closeFrame();
    }
    
    public void actionPerformed(ActionEvent e) {
        final String actionCommand = e.getActionCommand();
        if (ACCEPT_ACTION.equals(actionCommand)) {
            acceptCall();
        } else if (REJECT_ACTION.equals(actionCommand)) {
            rejectCall();
        } else if (HANGUP_ACTION.equals(actionCommand)) {
            hangup();
        }
    }

    public void update(Observable o, Object arg) {
        SipRequest sipRequest = (SipRequest) arg;
        
        SipHeaderFieldValue to = sipRequest.getSipHeaders().get(
                new SipHeaderFieldName(RFC3261.HDR_FROM));
        String remoteUri = to.getValue();
        if (remoteUri.indexOf(RFC3261.LEFT_ANGLE_BRACKET) > -1) {
            remoteUri = NameAddress.nameAddressToUri(remoteUri);
        }
        if (peer.equals(remoteUri)) {
            closeFrame();
        }
    }
    
    private void closeFrame() {
        if (frame != null) {
            frame.dispose();
            frame = null;
        }
        mainPanel = null;
        text = null;
        hangupButton = null;
        acceptButton = null;
        rejectButton = null;
    }
}
