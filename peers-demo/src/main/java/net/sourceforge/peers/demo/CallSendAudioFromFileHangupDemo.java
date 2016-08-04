package net.sourceforge.peers.demo;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.FileLogger;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.XmlConfig;
import net.sourceforge.peers.media.*;
import net.sourceforge.peers.rtp.RFC4733;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import java.io.File;
import java.util.Optional;

public class CallSendAudioFromFileHangupDemo implements SipListener {

    private Logger logger;
    private Config config;
    private UserAgent userAgent;
    private SipRequest sipRequest;

    private Object registeredSync = new Object();
    private Optional<Boolean> registered;

    public CallSendAudioFromFileHangupDemo(String calleeSipUrl) throws Exception {
        logger = new FileLogger(null);
        config = new XmlConfig(Utils.DEFAULT_PEERS_HOME + File.separator + UserAgent.CONFIG_FILE, logger);
        // Two consecutive calls. Two concurrent wont work - for now
        call(calleeSipUrl, "media/message.alaw", SoundSource.DataFormat.ALAW_8KHZ_MONO_LITTLE_ENDIAN);
        call(calleeSipUrl, "media/message.raw", SoundSource.DataFormat.LINEAR_PCM_8KHZ_16BITS_SIGNED_MONO_LITTLE_ENDIAN);
    }

    public void call(final String callee, final String mediaFile, final SoundSource.DataFormat mediaFileDataFormat) throws Exception {
        userAgent = new UserAgent(this, () -> new FilePlaybackSoundManager(mediaFile, mediaFileDataFormat, logger), config, null, logger);
        try {
            registered = Optional.empty();
            userAgent.register();
            if (!isRegistered()) {
                logger.error("Not able to register");
            }
            String callId = Utils.generateCallID(userAgent.getConfig().getLocalInetAddress());
            sipRequest = userAgent.invite(callee, callId);
            MediaManager mediaManager = userAgent.getMediaManager();
            mediaManager.waitConnected();
            SoundSource soundSource = mediaManager.getSoundSource();
            soundSource.waitFinished();
            mediaManager.waitFinishedSending();
            System.out.println("Hanging up");
            userAgent.terminate(sipRequest);
        } finally {
            userAgent.unregister();
            userAgent.close();
        }
    }

    // SipListener methods
    private boolean isRegistered() throws InterruptedException {
        if (registered.isPresent()) return registered.get();
        synchronized (registeredSync) {
            while (!registered.isPresent()) {
                registeredSync.wait();
            }
            return registered.get();
        }
    }
    
    @Override
    public void registering(SipRequest sipRequest) {
        System.out.println("Registering " + sipRequest);
    }

    @Override
    public void registerSuccessful(SipResponse sipResponse) {
        System.out.println("Register successful " + sipResponse);
        registered = Optional.of(true);
        synchronized (registeredSync) { registeredSync.notifyAll(); }
    }

    @Override
    public void registerFailed(SipResponse sipResponse) {
        System.out.println("Register failed " + sipResponse);
        registered = Optional.of(false);
        synchronized (registeredSync) { registeredSync.notifyAll(); }
    }

    @Override
    public void incomingCall(SipRequest sipRequest, SipResponse provResponse) { System.out.println("Incoming call " + sipRequest + ", " + provResponse); }

    @Override
    public void remoteHangup(SipRequest sipRequest) { System.out.println("Remote hangup " + sipRequest); }

    @Override
    public void ringing(SipResponse sipResponse) { System.out.println("Ringing " + sipResponse); }

    @Override
    public void calleePickup(SipResponse sipResponse) { System.out.println("Callee pickup " + sipResponse); }

    @Override
    public void error(SipResponse sipResponse) { System.out.println("Error " + sipResponse); }

    @Override
    public void dtmfEvent(RFC4733.DTMFEvent dtmfEvent, int duration) { System.out.println(" DTMF " + dtmfEvent.name()); }


    public static void main(String[] args) {
        try {
            new CallSendAudioFromFileHangupDemo(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
