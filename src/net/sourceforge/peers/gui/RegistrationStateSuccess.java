package net.sourceforge.peers.gui;

public class RegistrationStateSuccess extends RegistrationState {

    public RegistrationStateSuccess(String id, Registration registration) {
        super(id, registration);
    }

    @Override
    public void registerSent() {
        registration.setState(registration.REGISTERING);
        registration.displayRegistering();
    }

}
