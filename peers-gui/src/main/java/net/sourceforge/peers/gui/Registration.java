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

public class Registration {

    public final RegistrationState UNREGISTERED;
    public final RegistrationState REGISTERING;
    public final RegistrationState SUCCESS;
    public final RegistrationState FAILED;

    protected JLabel label;
    private RegistrationState state;

    public Registration(JLabel label, Logger logger) {
        this.label = label;

        String id = String.valueOf(hashCode());
        UNREGISTERED = new RegistrationStateUnregsitered(id, this, logger);
        state = UNREGISTERED;
        REGISTERING = new RegistrationStateRegistering(id, this, logger);
        SUCCESS = new RegistrationStateSuccess(id, this, logger);
        FAILED = new RegistrationStateFailed(id, this, logger);

    }

    public void setState(RegistrationState state) {
        this.state = state;
    }

    public synchronized void registerSent() {
        state.registerSent();
    }

    public synchronized void registerFailed() {
        state.registerFailed();
    }

    public synchronized void registerSuccessful() {
        state.registerSuccessful();
    }

    protected void displayRegistering() {
        URL url = getClass().getResource("working.gif");
//        String folder = MainFrame.class.getPackage().getName().replace(".",
//                File.separator);
//        String filename = folder + File.separator + "working.gif";
//        Logger.debug("filename: " + filename);
//        URL url = MainFrame.class.getClassLoader().getResource(filename);
        ImageIcon imageIcon = new ImageIcon(url);
        label.setIcon(imageIcon);
        label.setText("Registering");
    }

}
