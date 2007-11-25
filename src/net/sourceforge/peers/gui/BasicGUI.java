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

import net.sourceforge.peers.sip.core.useragent.UAC;
import net.sourceforge.peers.sip.core.useragent.UAS;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;

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
        new CallFrame(arg.toString());
    }
}
