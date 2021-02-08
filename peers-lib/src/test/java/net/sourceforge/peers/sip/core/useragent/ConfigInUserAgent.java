package net.sourceforge.peers.sip.core.useragent;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.JavaConfig;
import net.sourceforge.peers.media.AbstractSoundManager;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ConfigInUserAgent {

    @Test
    public void recordingDir() throws SocketException, UnknownHostException {
        String testPath = "this is a test";
        Config config = new JavaConfig();
        config.setLocalInetAddress(InetAddress.getLocalHost());

        AbstractSoundManager soundManager = new DummySoundManager();
        UserAgent testUser = new UserAgent(null, config, null,
                soundManager);

        //test path is not set yet
        Assert.assertEquals(testUser.getPeersHome() + File.separator
                + AbstractSoundManager.MEDIA_DIR + File.separator, testUser.getRecordingDir());

        //now it will have it set
        config.setMediaDir(testPath);
        Assert.assertEquals(testPath + File.separator, testUser.getRecordingDir());

    }

}
