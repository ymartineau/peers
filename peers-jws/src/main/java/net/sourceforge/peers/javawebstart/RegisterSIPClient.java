package net.sourceforge.peers.javawebstart;

import net.sourceforge.peers.FileLogger;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.javaxsound.JavaxSoundManager;
import net.sourceforge.peers.media.AbstractSoundManager;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import java.io.File;

public class RegisterSIPClient {
    private EventManager eventManager;
    private Registration registration;
    private Logger logger;
    public final static String CONFIG_FILE = "conf" + File.separator + "peers.xml";
    public RegisterSIPClient(final String[] args) {
        String peersHome = Utils.DEFAULT_PEERS_HOME;
        if (args.length > 0) {
            peersHome = args[0];
        }
        logger = new FileLogger(peersHome);
        registration = new Registration(logger);
        AbstractSoundManager soundManager = new JavaxSoundManager(false, logger, peersHome);;
//        if (config.getMicroPhoneEnable()) {
//            soundManager = new JavaxSoundManager(false, logger, peersHome);
//        }

        if (args.length > 0) {
            peersHome = args[0];
        }
        eventManager = new EventManager(RegisterSIPClient.this, peersHome, logger, soundManager);
        eventManager.register();
    }

    public void registerFailed(SipResponse sipResponse) {
        registration.registerFailed();
    }

    public void registerSuccessful(SipResponse sipResponse) {
        registration.registerSuccessful();
    }

    public void registering(SipRequest sipRequest) {
        registration.registerSent();
    }
}
