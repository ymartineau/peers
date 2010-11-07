package net.sourceforge.peers.gui;

import net.sourceforge.peers.sip.AbstractState;

public abstract class RegistrationState extends AbstractState {

    protected Registration registration;

    public RegistrationState(String id, Registration registration) {
        super(id);
        this.registration = registration;
    }

    public void registerSent() {}
    public void registerSuccessful() {}
    public void registerFailed() {}

}
