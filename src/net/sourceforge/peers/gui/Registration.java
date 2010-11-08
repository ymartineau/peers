package net.sourceforge.peers.gui;

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Registration {

    public final RegistrationState UNREGISTERED;
    public final RegistrationState REGISTERING;
    public final RegistrationState SUCCESS;
    public final RegistrationState FAILED;

    protected JLabel label;
    private RegistrationState state;

    public Registration(JLabel label) {
        this.label = label;

        String id = String.valueOf(hashCode());
        UNREGISTERED = new RegistrationStateUnregsitered(id, this);
        state = UNREGISTERED;
        REGISTERING = new RegistrationStateRegistering(id, this);
        SUCCESS = new RegistrationStateSuccess(id, this);
        FAILED = new RegistrationStateFailed(id, this);

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
