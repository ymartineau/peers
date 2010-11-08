package net.sourceforge.peers.gui;

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import net.sourceforge.peers.Logger;

public class RegistrationStateRegistering extends RegistrationState {

    public RegistrationStateRegistering(String id, Registration registration) {
        super(id, registration);
    }

    @Override
    public void registerSuccessful() {
        registration.setState(registration.SUCCESS);
        JLabel label = registration.label;
        URL url = getClass().getResource("green.png");
//        String folder = MainFrame.class.getPackage().getName().replace(".",
//                File.separator);
//        String filename = folder + File.separator + "green.png";
//        Logger.debug("filename: " + filename);
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
        Logger.debug("image url: " + url);
        ImageIcon imageIcon = new ImageIcon(url);
        label.setIcon(imageIcon);
        label.setText("Registration failed");
    }

}
