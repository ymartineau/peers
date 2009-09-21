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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class SipUA implements ActionListener {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    
    private static void createAndShowGUI() {
        JFrame.setDefaultLookAndFeelDecorated(true);
        new SipUA();
    }
    
    private JFrame mainFrame;
    private JMenuItem settingsIt;
    private JFrame settingsFrame;
    
    public SipUA() {
        mainFrame = new JFrame("Sip User-Agent");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        settingsIt = new JMenuItem("Settings");
        settingsIt.addActionListener(this);
        menu.add(settingsIt);
        mainFrame.setJMenuBar(menuBar);
        
        Container contentPane = mainFrame.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.TRAILING, 1, 2));
        JTextField uri = new JTextField("sip:500@ekiga.net", 15);
        
        JCheckBox audio = new JCheckBox("audio");
        topPanel.add(audio);
        
        JCheckBox video = new JCheckBox("video");
        topPanel.add(video);
        
        JCheckBox text = new JCheckBox("text");
        topPanel.add(text);
        
        JButton go = new JButton("GO");
        topPanel.add(go);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.PAGE_AXIS));
        
        String alice = "sip:alice@atlanta.com";
        JRadioButton aliceBt = new JRadioButton(alice);
        aliceBt.setActionCommand(alice);
        
        String bob = "sip:bob@biloxi.com";
        JRadioButton bobBt = new JRadioButton(bob);
        bobBt.setActionCommand(bob);
        
        String carol = "sip:carol@chicago.com";
        JRadioButton carolBt = new JRadioButton(carol);
        bobBt.setActionCommand(carol);
        
        ButtonGroup btGroup = new ButtonGroup();
        btGroup.add(aliceBt);
        btGroup.add(bobBt);
        btGroup.add(carolBt);
        
        list.add(aliceBt);
        list.add(bobBt);
        list.add(carolBt);
        
        contentPane.add(uri);
        contentPane.add(topPanel);
        contentPane.add(new JScrollPane(list));
        
        mainFrame.pack();
        mainFrame.setVisible(true);
    }
    
    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        if (settingsIt.equals(source)) {
            if (settingsFrame == null) {
                settingsFrame = new JFrame("Settings");
                settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                settingsFrame.getContentPane().add(new JLabel("TODO"));
                settingsFrame.pack();
            }
            settingsFrame.setVisible(true);
        }
    }

}
