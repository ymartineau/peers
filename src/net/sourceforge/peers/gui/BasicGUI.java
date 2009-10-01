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
    
    Copyright 2007, 2008, 2009 Yohann Martineau 
*/

package net.sourceforge.peers.gui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.SipEvent;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.core.useragent.SipEvent.EventType;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transport.SipMessage;
import net.sourceforge.peers.sip.transport.SipResponse;

public class BasicGUI implements ActionListener, Observer, WindowListener {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    
    private static void createAndShowGUI() {
        JFrame.setDefaultLookAndFeelDecorated(true);
        new BasicGUI();
    }

    private JFrame mainFrame;
    private JPanel mainPanel;
    private JTextField uri;
    private JButton actionButton;

    private UserAgent userAgent;

    public BasicGUI() {
        //create sip stack
        Thread thread = new Thread(new Runnable() {
           public void run() {
               userAgent = new UserAgent();
               userAgent.getUas().getInitialRequestManager()
                   .getInviteHandler().addObserver(BasicGUI.this);
           }
        });
        thread.start();
        
        mainFrame = new JFrame("Peers: SIP User-Agent");
//        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.addWindowListener(this);
        
        mainPanel = new JPanel();
        
        
        uri = new JTextField("sip:", 15);
        uri.addActionListener(this);
        actionButton = new JButton("Call");
        actionButton.addActionListener(this);
        
        mainPanel.add(uri);
        mainPanel.add(actionButton);
        
        Container contentPane = mainFrame.getContentPane();
        contentPane.add(mainPanel);
        
        mainFrame.pack();
        mainFrame.setVisible(true);

    }

    //////////////////////////////////////////////////////////
    // ActionListener methods
    //////////////////////////////////////////////////////////
    
    public void actionPerformed(ActionEvent e) {
        final String sipUri = uri.getText();
        final String callId = Utils.generateCallID(userAgent.getMyAddress());
        SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    new CallFrame(sipUri, callId, userAgent);
                    userAgent.getUac().invite(sipUri, callId);
                } catch (SipUriSyntaxException e) {
                    Logger.error("syntax issue", e);
                }
                return null;
            }
        };
        swingWorker.execute();
        
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                
//            }
//        });
    }

    //////////////////////////////////////////////////////////
    // Observer methods
    //////////////////////////////////////////////////////////
    
    public void update(Observable o, Object arg) {
        if (arg instanceof SipEvent) {
            SipEvent sipEvent = (SipEvent) arg;
            if (sipEvent.getEventType() == EventType.INCOMING_CALL) {
                final SipMessage sipMessage = sipEvent.getSipMessage();
                if (sipMessage instanceof SipResponse) {
                    CallFrameRunnable callFrameRunnable =
                        new CallFrameRunnable((SipResponse)sipMessage);
                    SwingUtilities.invokeLater(callFrameRunnable);
                    while (!callFrameRunnable.isCallFrameCreated()) {
                        try {
                            Thread.sleep(15);
                        } catch (InterruptedException e) {
                            Logger.debug("basic gui sleep interrupted");
                            break;
                        }
                    }
                }
            }
        }
    }

    //////////////////////////////////////////////////////////
    // WindowAdapter methods
    //////////////////////////////////////////////////////////
    
    public void windowActivated(WindowEvent arg0) {
    }

    public void windowClosed(WindowEvent arg0) {
        try {
            userAgent.getUac().unregister();
        } catch (Exception e) {
            Logger.error("error while unregistering", e);
        }
        System.exit(0);
    }

    public void windowClosing(WindowEvent arg0) {
    }

    public void windowDeactivated(WindowEvent arg0) {
    }

    public void windowDeiconified(WindowEvent arg0) {
    }

    public void windowIconified(WindowEvent arg0) {
    }

    public void windowOpened(WindowEvent arg0) {
    }

    private class CallFrameRunnable implements Runnable {

        private boolean callFrameCreated;
        private SipResponse sipResponse;

        private CallFrameRunnable(SipResponse sipResponse) {
            this.sipResponse = sipResponse;
            callFrameCreated = false;
        }

        @Override
        public void run() {
            Logger.debug("running new call frame");
            new CallFrame(sipResponse, userAgent);
            callFrameCreated = true;
        }

        public boolean isCallFrameCreated() {
            return callFrameCreated;
        }

    }
}
