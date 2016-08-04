package net.sourceforge.peers.media;

import net.sourceforge.peers.Logger;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;

public class NoEncodingEncoder extends Encoder {

    public NoEncodingEncoder(PipedInputStream rawData, PipedOutputStream encodedData, boolean mediaDebug, Logger logger, String peersHome, CountDownLatch latch) {
        super(rawData, encodedData, mediaDebug, logger, peersHome, latch);
    }

    @Override
    public byte[] process(byte[] media, int len) {
        return media;
    }

}
