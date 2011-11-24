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

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import net.sourceforge.peers.Logger;

public class RegistrationStateRegistering extends RegistrationState {

    public RegistrationStateRegistering(String id, Registration registration,
            Logger logger) {
        super(id, registration, logger);
    }

    @Override
    public void registerSuccessful() {
        registration.setState(registration.SUCCESS);
        JLabel label = registration.label;
        URL url = getClass().getResource("green.png");
//        String folder = MainFrame.class.getPackage().getName().replace(".",
//                File.separator);
//        String filename = folder + File.separator + "green.png";
//        logger.debug("filename: " + filename);
//        URL url = MainFrame.class.getClassLoader().getResource(filename);
        ImageIcon imageIcon = new ImageIcon(url);
        label.setIcon(imageIcon);
        label.setText("Registered");
    }

    @Override
    public void registerFailed() {
        registration.setState(registration.FAILED);
        JLabel label = registration.label;
        URL url = getClass().getResource("red.png");
//        String folder = MainFrame.class.getPackage().getName().replace(".",
//                File.separator);
//        URL url = MainFrame.class.getClassLoader().getResource(
//                folder + File.separator + "red.png");
        logger.debug("image url: " + url);
        ImageIcon imageIcon = new ImageIcon(url);
        label.setIcon(imageIcon);
        label.setText("Registration failed");
    }

}
