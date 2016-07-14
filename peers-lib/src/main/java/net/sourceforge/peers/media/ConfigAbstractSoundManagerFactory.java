package net.sourceforge.peers.media;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.media.javaxsound.JavaxSoundManager;

public class ConfigAbstractSoundManagerFactory implements AbstractSoundManagerFactory {

    private Config config;
    private String peersHome;
    private Logger logger;

    public ConfigAbstractSoundManagerFactory(Config config, String peersHome, Logger logger) {
        this.config = config;
        this.peersHome = peersHome;
        this.logger = logger;
    }

    @Override
    public AbstractSoundManager getSoundManager()  {
        switch (config.getMediaMode()) {
            case captureAndPlayback:
                return new JavaxSoundManager(config.isMediaDebug(), logger, peersHome);
            case echo:
                return null;
            case file:
                return new FilePlaybackSoundManager(config.getMediaFile(), config.getMediaFileDataFormat(), logger);
            case none:
            default:
                return null;
        }
    }

}
