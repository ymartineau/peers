package net.sourceforge.peers.sip.core.useragent;

import net.sourceforge.peers.media.AbstractSoundManager;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

public class DummySipListener implements SipListener {

    private AbstractSoundManager soundManager;

    public DummySipListener(AbstractSoundManager soundManager) {
        this.soundManager = soundManager;
    }

    @Override
    public void registering(SipRequest sipRequest) {}

    @Override
    public void registerSuccessful(SipResponse sipResponse) {}

    @Override
    public void registerFailed(SipResponse sipResponse) {}

    @Override
    public void incomingCall(SipRequest sipRequest, SipResponse provResponse) {}

    @Override
    public void remoteHangup(SipRequest sipRequest) {}

    @Override
    public void ringing(SipResponse sipResponse) {}

    @Override
    public void calleePickup(SipResponse sipResponse) {}

    @Override
    public AbstractSoundManager getSoundManager() {
        return soundManager;
    }

    @Override
    public void error(SipResponse sipResponse) {}
}
