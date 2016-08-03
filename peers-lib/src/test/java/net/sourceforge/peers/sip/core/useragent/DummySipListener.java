package net.sourceforge.peers.sip.core.useragent;

import net.sourceforge.peers.rtp.RFC4733;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

public class DummySipListener implements SipListener {

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
    public void error(SipResponse sipResponse) {}

    @Override
    public void dtmfEvent(RFC4733.DTMFEvent dtmfEvent, int duration) {}
}
