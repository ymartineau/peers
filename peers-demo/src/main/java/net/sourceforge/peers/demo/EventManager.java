package net.sourceforge.peers.demo;

import java.net.SocketException;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.FileLogger;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.javaxsound.JavaxSoundManager;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

public class EventManager implements SipListener {

    //TODO add to doc
    private UserAgent userAgent;
    
    public EventManager() throws SocketException {
        Config config = new MyConfig();
        Logger logger = new FileLogger(null);
        JavaxSoundManager javaxSoundManager = new JavaxSoundManager(false, logger, null);
        userAgent = new UserAgent(this, config, logger, javaxSoundManager);
    }
    //end TODO add to doc
    
    
    // commands methods
    public void call(String callee) { }
    public void hangup() { }
    
    
    // SipListener methods
    
    @Override
    public void registering(SipRequest sipRequest) { }

    @Override
    public void registerSuccessful(SipResponse sipResponse) { }

    @Override
    public void registerFailed(SipResponse sipResponse) { }

    @Override
    public void incomingCall(SipRequest sipRequest, SipResponse provResponse) { }

    @Override
    public void remoteHangup(SipRequest sipRequest) { }

    @Override
    public void ringing(SipResponse sipResponse) { }

    @Override
    public void calleePickup(SipResponse sipResponse) { }

    @Override
    public void error(SipResponse sipResponse) { }

}
