package net.sourceforge.peers.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sourceforge.peers.sip.core.useragent.UAC;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.transactionuser.Dialog;

public class CallFrame implements ActionListener {

    private JFrame frame;
    private JPanel mainPanel;
    private JLabel text;
    private JButton hangupButton;
    
    public CallFrame(String peer) {
        frame = new JFrame(peer);
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
    }

    public void actionPerformed(ActionEvent e) {
        String peer = e.getActionCommand();
        Thread hangupThread = new Thread(peer) {
            @Override
            public void run() {
                Dialog dialog = UserAgent.getInstance().getDialog(getName());
                UAC.getInstance().terminate(dialog);
            }
        };
        hangupThread.start();
        frame.dispose();
        frame = null;
        mainPanel = null;
        text = null;
        hangupButton = null;
    }
    
}
