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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
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
        int buf_size = Capture.BUFFER_SIZE / 2;
        byte[] buffer = new byte[buf_size];
        int timestamp = 0;
        int numBytesRead;
        int tempBytesRead;
        long sleepTime = 0;
        long offset = 0;
        long lastSentTime = System.nanoTime();
        // indicate if its the first time that we send a packet (dont wait)
        int firstTimes = 0;
        while (!isStopped) {
            numBytesRead = 0;
            try {
                while (!isStopped && numBytesRead < buf_size) {
                    // expect that the buffer is full
                    tempBytesRead = encodedData.read(buffer, numBytesRead,
                            buf_size - numBytesRead);
                    numBytesRead += tempBytesRead;
                }
            } catch (IOException e) {
                logger.error("input/output error", e);
                return;
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
                    break;
                }
            }
            if (pushedPackets.size() > 0) {
                RtpPacket pushedPacket = pushedPackets.remove(0);
                rtpPacket.setMarker(pushedPacket.isMarker());
                rtpPacket.setPayloadType(pushedPacket.getPayloadType());
                rtpPacket.setIncrementTimeStamp(pushedPacket.isIncrementTimeStamp());
                byte[] data = pushedPacket.getData();
                rtpPacket.setData(data);
            } else {
                if (rtpPacket.getPayloadType() != codec.getPayloadType()) {
                    rtpPacket.setPayloadType(codec.getPayloadType());
                    rtpPacket.setMarker(false);
                }
                rtpPacket.setData(trimmedBuffer);
            }
            
            rtpPacket.setSequenceNumber(sequenceNumber++);
            if (rtpPacket.isIncrementTimeStamp()) {
                    timestamp += buf_size;
                }
            rtpPacket.setTimestamp(timestamp);
            if (firstTimes < 15) {
                rtpSession.send(rtpPacket);
                lastSentTime = System.nanoTime();
                firstTimes++;
                continue;
            }
            // todo改成config里取
            sleepTime = 19500000 - (System.nanoTime() - lastSentTime) + offset;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(Math.round(sleepTime / 1000000f));
                } catch (InterruptedException e) {
                    logger.error("Thread interrupted", e);
                    return;
                }
                rtpSession.send(rtpPacket);
                lastSentTime = System.nanoTime();
                offset = 0;
            } else {
                rtpSession.send(rtpPacket);
                lastSentTime = System.nanoTime();
                if (sleepTime < -20000000) {
                    offset = sleepTime + 20000000;
                }
            }
        }
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

    public synchronized void setStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }

    public void pushPackets(List<RtpPacket> rtpPackets) {
        this.pushedPackets.addAll(rtpPackets);
    }

}
