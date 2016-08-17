/*
    This file is part of Peers, a java SIP softphone.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    Copyright 2008, 2009, 2010, 2011 Yohann Martineau 
*/

package net.sourceforge.peers.media;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.rtp.RtpPacket;
import net.sourceforge.peers.rtp.RtpSession;
import net.sourceforge.peers.sdp.Codec;

public class RtpSender implements Runnable {

    private static int BUF_SIZE = Capture.BUFFER_SIZE / 2;
    public static int CONSUMING_BYTES_PER_MS = BUF_SIZE / 20; // Consuming BUF_SIZE bytes every 20 ms

    private PipedInputStream encodedData;
    private RtpSession rtpSession;
    private boolean isStopped;
    private FileOutputStream rtpSenderInput;
    private boolean mediaDebug;
    private Codec codec;
    private List<RtpPacket> pushedPackets;
    private Logger logger;
    private String peersHome;
    private CountDownLatch latch;
    
    public RtpSender(PipedInputStream encodedData, RtpSession rtpSession,
            boolean mediaDebug, Codec codec, Logger logger, String peersHome,
            CountDownLatch latch) {
        this.encodedData = encodedData;
        this.rtpSession = rtpSession;
        this.mediaDebug = mediaDebug;
        this.codec = codec;
        this.peersHome = peersHome;
        this.latch = latch;
        this.logger = logger;
        isStopped = false;
        pushedPackets = Collections.synchronizedList(
                new ArrayList<RtpPacket>());
    }

    public void run() {
        try {
            if (mediaDebug) {
                SimpleDateFormat simpleDateFormat =
                        new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String date = simpleDateFormat.format(new Date());
                String fileName = peersHome + File.separator
                        + AbstractSoundManager.MEDIA_DIR + File.separator + date
                        + "_rtp_sender.input";
                try {
                    rtpSenderInput = new FileOutputStream(fileName);
                } catch (FileNotFoundException e) {
                    logger.error("cannot create file", e);
                    return;
                }
            }
            RtpPacket rtpPacket = new RtpPacket();
            rtpPacket.setVersion(2);
            rtpPacket.setPadding(false);
            rtpPacket.setExtension(false);
            rtpPacket.setCsrcCount(0);
            rtpPacket.setMarker(false);
            rtpPacket.setPayloadType(codec.getPayloadType());
            Random random = new Random();
            int sequenceNumber = random.nextInt();
            rtpPacket.setSequenceNumber(sequenceNumber);
            rtpPacket.setSsrc(random.nextInt());
            byte[] buffer = new byte[BUF_SIZE];
            int timestamp = 0;
            int numBytesRead;
            int tempBytesRead;
            long sleepTime = 0;
            long lastSentTime = System.nanoTime();
            // indicate if its the first time that we send a packet (dont wait)
            boolean firstTime = true;

            int sleeps = 0;
            long sumOversleep = 0;
            long avgOversleep = 0;
            while (!isStopped || encodedDataAvailable() > 0 || pushedPackets.size() > 0) {
                if (pushedPackets.size() > 0) {
                    RtpPacket pushedPacket = pushedPackets.remove(0);
                    rtpPacket.setMarker(pushedPacket.isMarker());
                    rtpPacket.setPayloadType(pushedPacket.getPayloadType());
                    rtpPacket.setIncrementTimeStamp(pushedPacket.isIncrementTimeStamp());
                    byte[] data = pushedPacket.getData();
                    rtpPacket.setData(data);
                } else {
                    numBytesRead = 0;
                    try {
                        while (numBytesRead < BUF_SIZE) {
                            // expect that the buffer is full
                            tempBytesRead = encodedData.read(buffer, numBytesRead, BUF_SIZE - numBytesRead);
                            if (tempBytesRead < 0) {
                                setStopped(true);
                                break;
                            }
                            numBytesRead += tempBytesRead;
                        }
                    } catch (IOException e) {
                        // Getting an IOException reading from rawData is expected after the encoder has been stopped
                        if (!isStopped) logger.error("Error reading encoded data", e);
                    }

                    byte[] trimmedBuffer;
                    if (numBytesRead < buffer.length) {
                        trimmedBuffer = new byte[numBytesRead];
                        System.arraycopy(buffer, 0, trimmedBuffer, 0, numBytesRead);
                    } else {
                        trimmedBuffer = buffer;
                    }
                    if (mediaDebug) {
                        try {
                            rtpSenderInput.write(trimmedBuffer); // TODO use classpath
                        } catch (IOException e) {
                            logger.error("cannot write to file", e);
                        }
                    }

                    if (rtpPacket.getPayloadType() != codec.getPayloadType()) {
                        rtpPacket.setPayloadType(codec.getPayloadType());
                        rtpPacket.setMarker(false);
                    }
                    rtpPacket.setData(trimmedBuffer);
                }

                rtpPacket.setSequenceNumber(sequenceNumber++);
                if (rtpPacket.isIncrementTimeStamp()) {
                    timestamp += BUF_SIZE;
                }
                rtpPacket.setTimestamp(timestamp);
                if (firstTime) {
                    lastSentTime = System.nanoTime();
                    rtpSession.send(rtpPacket);
                    firstTime = false;
                    continue;
                }
                long beforeSleep = System.nanoTime();
                sleepTime = 20000000 - (beforeSleep - lastSentTime) - avgOversleep;
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime / 1000000, (int) sleepTime % 1000000);
                    } catch (InterruptedException e) {
                        logger.error("Thread interrupted", e);
                        return;
                    }
                    lastSentTime = System.nanoTime();
                    long slept = (lastSentTime - beforeSleep);
                    long oversleep = slept - sleepTime;
                    sumOversleep += oversleep;
                    if (sleeps++ == 10) {
                        avgOversleep = (sumOversleep / sleeps);
                        sleeps = 0;
                        sumOversleep = 0;
                    }
                    rtpSession.send(rtpPacket);
                } else {
                    lastSentTime = System.nanoTime();
                    rtpSession.send(rtpPacket);
                }
            }
        } finally {
            if (mediaDebug) {
                try {
                    rtpSenderInput.close();
                } catch (IOException e) {
                    logger.error("cannot close file", e);
                    return;
                }
            }
            latch.countDown();
            if (latch.getCount() != 0) {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    logger.error("interrupt exception", e);
                }
            }
        }
    }

    public synchronized void setStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }

    public void waitEmpty() throws IOException, InterruptedException {
        // FIXME This is the poor mans waiting. Really ought to be able to do it blocking. Besides that available() cannot really be trusted - it may
        // return 0 even though data is in the pipe arriving soon
        while (encodedData.available() > 0) {
            Thread.sleep(5);
        }
    }

    public void pushPackets(List<RtpPacket> rtpPackets) {
        this.pushedPackets.addAll(rtpPackets);
    }

    private int encodedDataAvailable() {
        try {
            return encodedData.available();
        } catch (IOException e) {
            // PipedInputStream.available never throws IOException in practice
            logger.error("Error getting amount available encoded data. Should never happen", e);
            return 0;
        }
    }


}
