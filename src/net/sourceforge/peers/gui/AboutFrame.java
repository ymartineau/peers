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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;

import net.sourceforge.peers.Logger;

public class AboutFrame extends JFrame implements ActionListener,
        HyperlinkListener {

    public static final String LICENSE_FILE = File.separator + "gpl.txt";

    private static final long serialVersionUID = 1L;

    private Logger logger;

    public AboutFrame(String peersHome, Logger logger) {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("About");

        String message = "Peers: java SIP softphone<br>"
            + "Copyright 2007-2010 Yohann Martineau<br>"
            + "<a href=\"" + EventManager.PEERS_URL + "\">"
            + EventManager.PEERS_URL + "</a>";
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setText(message);
        textPane.addHyperlinkListener(this);
        textPane.setOpaque(false);
        textPane.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        add(textPane, BorderLayout.PAGE_START);
        String gpl = null;
        try {
            FileReader fileReader = new FileReader(peersHome + LICENSE_FILE);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuffer buf = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                buf.append("  ");
                buf.append(line);
                buf.append("\r\n");
            }
            gpl = buf.toString();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        Font font = textArea.getFont();
        font = new Font(font.getName(), font.getStyle(), font.getSize() - 2);
        textArea.setFont(font);
        if (gpl != null) {
            textArea.setText(gpl);
        }
        JPanel panel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setCaretPosition(0);
        //scrollPane.setPreferredSize(new Dimension(600, 300));
        panel.add(scrollPane);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add(panel, BorderLayout.CENTER);

        panel = new JPanel();
        JButton button = new JButton("Close");
        button.addActionListener(this);
        panel.add(button);
        add(panel, BorderLayout.PAGE_END);

        pack();

        Dimension dimension = scrollPane.getSize();
        dimension = new Dimension(dimension.width + 20, 300);
        scrollPane.setPreferredSize(dimension);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        dispose();
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
        if (EventType.ACTIVATED.equals(hyperlinkEvent.getEventType())) {
            try {
                URI uri = hyperlinkEvent.getURL().toURI();
                java.awt.Desktop.getDesktop().browse(uri);
            } catch (URISyntaxException e) {
                logger.error(e.getMessage(), e);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
