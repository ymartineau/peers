package net.sourceforge.peers.gui;

public class RegistrationStateFailed extends RegistrationState {

    public RegistrationStateFailed(String id, Registration registration) {
        super(id, registration);
    }

    @Override
    public void registerSent() {
        registration.setState(registration.REGISTERING);
        registration.displayRegistering();
    }

}
