package net.sourceforge.peers.media;

import net.sourceforge.peers.rtp.RFC4733;

public interface DtmfEventHandler {
    public void dtmfDetected(RFC4733.DTMFEvent dtmfEvent, int duration);
}
