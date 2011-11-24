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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;


public class Keypad extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;

    public static final String CHARS = "123456789*0#";

    private CallFrame callFrame;

    public Keypad(CallFrame callFrame) {
        this.callFrame = callFrame;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridLayout(4, 3, 10, 5));
        for (int i = 0; i < CHARS.length(); ++i) {
            char[] c = { CHARS.charAt(i) };
            String digit = new String(c);
            JButton button = new JButton(digit);
            button.setActionCommand(digit);
            button.addActionListener(this);
            add(button);
        }
        setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
        Dimension dimension = new Dimension(180, 115);
        setMinimumSize(dimension);
        setMaximumSize(dimension);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String command = actionEvent.getActionCommand();
        callFrame.keypadEvent(command.charAt(0));
    }

}
