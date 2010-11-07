package net.sourceforge.peers.gui;


public class RegistrationStateUnregsitered extends RegistrationState {

    public RegistrationStateUnregsitered(String id, Registration registration) {
        super(id, registration);
    }

    @Override
    public void registerSent() {
        registration.setState(registration.REGISTERING);
        registration.displayRegistering();
    }

}
