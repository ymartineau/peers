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

import net.sourceforge.peers.sip.core.useragent.UAC;
import net.sourceforge.peers.sip.core.useragent.UAS;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.transactionuser.Dialog;

public class CallFrame implements ActionListener, Observer {

    private String peer;
    private JFrame frame;
    private JPanel mainPanel;
    private JLabel text;
    private JButton hangupButton;
    
    public CallFrame(String peer) {
        this.peer = peer;
        frame = new JFrame(peer);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                actionPerformed(null);
            }
        });
        mainPanel = new JPanel();
        text = new JLabel("Session with: " + peer);
        hangupButton = new JButton("Hang up");
        hangupButton.setActionCommand(peer);
        hangupButton.addActionListener(this);
        
        mainPanel.add(text);
        mainPanel.add(hangupButton);
        
        frame.getContentPane().add(mainPanel);
        
        frame.pack();
        frame.setVisible(true);
        
        UAS.getInstance().getMidDialogRequestManager().getByeHandler().addObserver(this);
    }

    public void actionPerformed(ActionEvent e) {
        Thread hangupThread = new Thread() {
            @Override
            public void run() {
                Dialog dialog = UserAgent.getInstance().getDialog(peer);
                UAC.getInstance().terminate(dialog);
            }
        };
        hangupThread.start();
        closeFrame();
    }

    public void update(Observable o, Object arg) {
        if (peer.equals(arg.toString())) {
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
    }
}
