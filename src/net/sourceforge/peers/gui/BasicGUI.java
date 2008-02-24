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

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.core.useragent.UAC;
import net.sourceforge.peers.sip.core.useragent.UAS;
import net.sourceforge.peers.sip.core.useragent.handlers.InviteHandler;
import net.sourceforge.peers.sip.syntaxencoding.NameAddress;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transactionuser.DialogStateEarly;
import net.sourceforge.peers.sip.transport.SipRequest;

public class BasicGUI implements ActionListener, Observer {

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
    
    public BasicGUI() {
        mainFrame = new JFrame("Peers: SIP User-Agent");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        mainPanel = new JPanel();
        
        
        uri = new JTextField("sip:192.168.1.2", 15);
        actionButton = new JButton("Call");
        actionButton.addActionListener(this);
        
        mainPanel.add(uri);
        mainPanel.add(actionButton);
        
        Container contentPane = mainFrame.getContentPane();
        contentPane.add(mainPanel);
        
        mainFrame.pack();
        mainFrame.setVisible(true);
        //create sip stack
        UAS uas = UAS.getInstance();
        uas.getInitialRequestManager().getInviteHandler().addObserver(this);
        UAC.getInstance();
        
    }

    public void actionPerformed(ActionEvent e) {
        String sipUri = uri.getText();
        Thread callThread = new Thread(sipUri) {
            @Override
            public void run() {
                try {
                    UAC.getInstance().invite(getName());
                } catch (SipUriSyntaxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        callThread.start();
        new CallFrame(sipUri);
    }

    public void update(Observable o, Object arg) {
        SipRequest sipRequest = (SipRequest) arg;
        
        SipHeaderFieldValue to = sipRequest.getSipHeaders().get(
                new SipHeaderFieldName(RFC3261.HDR_FROM));
        String remoteUri = to.getValue();
        if (remoteUri.indexOf(RFC3261.LEFT_ANGLE_BRACKET) > -1) {
            remoteUri = NameAddress.nameAddressToUri(remoteUri);
        }
        Dialog dialog = DialogManager.getInstance().getDialog(remoteUri);
        if (dialog.getState() instanceof DialogStateEarly) {
            new CallFrame((InviteHandler) o, sipRequest);
        }
    }
}
