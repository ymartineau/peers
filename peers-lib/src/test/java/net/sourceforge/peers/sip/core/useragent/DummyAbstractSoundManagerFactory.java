package net.sourceforge.peers.sip.core.useragent;

import net.sourceforge.peers.media.AbstractSoundManager;
import net.sourceforge.peers.media.AbstractSoundManagerFactory;

public class DummyAbstractSoundManagerFactory implements AbstractSoundManagerFactory {

    private DummySoundManager soundManager = new DummySoundManager();

    @Override
    public AbstractSoundManager getSoundManager() {
        return soundManager;
    }

}
