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

package net.sourceforge.peers.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.JavaUtils;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.SipEvent;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.core.useragent.SipEvent.EventType;
import net.sourceforge.peers.sip.core.useragent.handlers.InviteHandler;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogState;
import net.sourceforge.peers.sip.transactionuser.DialogStateConfirmed;
import net.sourceforge.peers.sip.transactionuser.DialogStateEarly;
import net.sourceforge.peers.sip.transactionuser.DialogStateTerminated;
import net.sourceforge.peers.sip.transport.SipMessage;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

public class CallFrame implements ActionListener, Observer {

    //GUI strings
    public static final String ACCEPT_ACTION = "Accept";
    public static final String BYE_ACTION    = "Bye";
    public static final String CANCEL_ACTION = "Cancel";
    public static final String CLOSE_ACTION  = "Close";
    public static final String REJECT_ACTION = "Reject";
    
    //GUI objects
    private JFrame frame;
    private JPanel mainPanel;
    private JLabel text;
    private JButton acceptButton;
    private JButton byeButton;
    private JButton cancelButton;
    private JButton closeButton;
    private JButton rejectButton;
    
    //sip stack objects
    private String callId;
    private boolean isUac;
    private Dialog dialog;
    private SipRequest sipRequest;
    private InviteHandler inviteHandler;
    
    private UserAgent userAgent;
    
    //for uac
    public CallFrame(String requestUri, String callId, UserAgent userAgent) {
        isUac = true;
        this.callId = callId;
        this.userAgent = userAgent;
        inviteHandler = userAgent.getUac().getInviteHandler();
        
        frame = new JFrame(requestUri);
        //TODO window listener
//        frame.addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosing(WindowEvent we) {
//                closeFrame();
//            }
//        });
        mainPanel = new JPanel();
        text = new JLabel("Calling " + requestUri);
        closeButton = new JButton(CLOSE_ACTION);
        closeButton.setActionCommand(CLOSE_ACTION);
        closeButton.addActionListener(this);
        
        mainPanel.add(text);
        mainPanel.add(closeButton);
        
        frame.getContentPane().add(mainPanel);
        
        frame.pack();
        frame.setVisible(true);
        
        inviteHandler.addObserver(this);
    }
    
    //for uas
    public CallFrame(SipResponse sipResponse, UserAgent userAgent) {
        isUac = false;
        sipRequest = userAgent.getSipRequest(sipResponse);
        dialog = userAgent.getDialogManager().getDialog(sipResponse);
        dialog.addObserver(this);
        callId = dialog.getCallId();
        this.userAgent = userAgent;
        inviteHandler = userAgent.getUas()
            .getInitialRequestManager().getInviteHandler();
    }

    private void acceptCall() {
        SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                //FIXME redisgn interface, only use useragent
                inviteHandler.acceptCall(sipRequest, dialog);
                return null;
            }
        };
        swingWorker.execute();
    }
    
    private void rejectCall() {
        SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                inviteHandler.rejectCall(sipRequest);
                return null;
            }
        };
        swingWorker.execute();
    }
    
    private void cancel() {
        SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                userAgent.getUac().terminate(dialog, sipRequest);
                return null;
            }
        };
        swingWorker.execute();
    }
    
    private void hangup() {
        SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                userAgent.getUac().terminate(dialog);
                return null;
            }
        };
        swingWorker.execute();
    }
    
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        Logger.debug(callId + " action performed: " + actionCommand);
        if (CLOSE_ACTION.equals(actionCommand)) {
            hangup();
        } else if (CANCEL_ACTION.equals(actionCommand)) {
            cancel();
        } else if (ACCEPT_ACTION.equals(actionCommand)) {
            acceptCall();
        } else if (REJECT_ACTION.equals(actionCommand)) {
            rejectCall();
            closeFrame();
        } else if (BYE_ACTION.equals(actionCommand)) {
            hangup();
            closeFrame();
        }
    }

    public void update(Observable o, Object arg) {
        Logger.debug("update with observable " + o + " arg = " + arg);
        if (o.equals(inviteHandler)) {
            if (arg instanceof SipEvent) {
                SipEvent sipEvent = (SipEvent) arg;
                SipMessage sipMessage = sipEvent.getSipMessage();
                if (Utils.getMessageCallId(sipMessage).equals(callId)) {
                    manageInviteHandlerEvent(sipEvent);
                }
                //if event is not for this frame (conversation) simply discard it
            } else {
                System.err.println("invite handler notification unknown");
            }
        } else if (o instanceof Dialog) {
            if (dialog == null) {
                dialog = (Dialog) o;
            }
            if (arg instanceof DialogState) {
                DialogState dialogState = (DialogState) arg;
                updateGui(dialogState);
            } else {
                System.err.println("dialog notification unknown");
            }
        }
    }
    
    private void manageInviteHandlerEvent(SipEvent sipEvent) {
        EventType eventType = sipEvent.getEventType();
        switch (eventType) {
        
        case RINGING:
            dialog = userAgent.getDialogManager().getDialog(sipEvent.getSipMessage());
            sipRequest = userAgent.getSipRequest(sipEvent.getSipMessage());
            dialog.addObserver(this);
            break;
            
        case CALLEE_PICKUP:
            dialog = userAgent.getDialogManager().getDialog(sipEvent.getSipMessage());
            dialog.addObserver(this);
            break;
            
        case ERROR:
            closeFrame();
            break;

        default:
            System.err.println("unknown sip event: " + sipEvent);
            break;
        }
    }
    
    private void updateGui(DialogState dialogState) {
        //TODO use a real state machine
        //state.setText(JavaUtils.getShortClassName(dialogState.getClass()));
        StringBuffer buf = new StringBuffer();
        buf.append("updateGui ");
        buf.append(dialog.getId());
        buf.append(" [");
        buf.append(JavaUtils.getShortClassName(dialog.getState().getClass()));
        buf.append(" -> ");
        buf.append(JavaUtils.getShortClassName(dialogState.getClass()));
        buf.append("]");
        Logger.debug(buf.toString());
        if (dialogState instanceof DialogStateEarly) {
            if (isUac && cancelButton == null) {
                //TODO implement cancel in core
                text.setText("Ringing " + dialog.getRemoteUri());
                cancelButton = new JButton(CANCEL_ACTION);
                cancelButton.setActionCommand(CANCEL_ACTION);
                cancelButton.addActionListener(this);
                mainPanel.remove(closeButton);
                mainPanel.add(cancelButton);
                frame.pack();
            } else {
                frame = new JFrame(dialog.getRemoteUri());
                mainPanel = new JPanel();
                text = new JLabel("Incoming call from " + dialog.getRemoteUri());
                acceptButton = new JButton(ACCEPT_ACTION);
                acceptButton.setActionCommand(ACCEPT_ACTION);
                acceptButton.addActionListener(this);
                rejectButton = new JButton(REJECT_ACTION);
                rejectButton.setActionCommand(REJECT_ACTION);
                rejectButton.addActionListener(this);
                mainPanel.add(text);
                mainPanel.add(acceptButton);
                mainPanel.add(rejectButton);
                frame.getContentPane().add(mainPanel);
                frame.pack();
                frame.setVisible(true);
            }
        } else if (dialogState instanceof DialogStateConfirmed) {
            //TODO create hangup button and remove previous buttons for both uac and uas
            text.setText("Talk to " + dialog.getRemoteUri());
            byeButton = new JButton(BYE_ACTION);
            byeButton.setActionCommand(BYE_ACTION);
            byeButton.addActionListener(this);
            if (isUac) {
                mainPanel.remove(closeButton);
                closeButton = null;
                if (cancelButton != null) {
                    mainPanel.remove(cancelButton);
                }
                cancelButton = null;
            } else {
                mainPanel.remove(acceptButton);
                acceptButton = null;
                if (rejectButton != null) {
                    mainPanel.remove(rejectButton);
                }
                rejectButton = null;
            }
            mainPanel.add(byeButton);
            frame.pack();
        } else if (dialogState instanceof DialogStateTerminated) {
            //TODO close frame for both uac and uas
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
        rejectButton = null;
        acceptButton = null;
        closeButton = null;
        cancelButton = null;
        byeButton = null;
    }
    
}
