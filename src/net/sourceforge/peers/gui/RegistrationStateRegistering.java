package net.sourceforge.peers.gui;

import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class RegistrationStateRegistering extends RegistrationState {

    public RegistrationStateRegistering(String id, Registration registration) {
        super(id, registration);
    }

    @Override
    public void registerSuccessful() {
        registration.setState(registration.SUCCESS);
        JLabel label = registration.label;
        String folder = MainFrame.class.getPackage().getName().replace(".",
                File.separator);
        URL url = MainFrame.class.getClassLoader().getResource(
                folder + File.separator + "green.png");
        ImageIcon imageIcon = new ImageIcon(url);
        label.setIcon(imageIcon);
        label.setText("Registered");
    }

    @Override
    public void registerFailed() {
        registration.setState(registration.FAILED);
        JLabel label = registration.label;
        String folder = MainFrame.class.getPackage().getName().replace(".",
                File.separator);
        URL url = MainFrame.class.getClassLoader().getResource(
                folder + File.separator + "red.png");
        ImageIcon imageIcon = new ImageIcon(url);
        label.setIcon(imageIcon);
        label.setText("Registration failed");
    }

}
