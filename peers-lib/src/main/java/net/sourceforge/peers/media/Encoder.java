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
import java.io.PipedOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import net.sourceforge.peers.Logger;


public abstract class Encoder implements Runnable {
    
    private PipedInputStream rawData;
    private PipedOutputStream encodedData;
    private boolean isStopped;
    private FileOutputStream encoderOutput;
    private FileOutputStream encoderInput;
    private boolean mediaDebug;
    private Logger logger;
    private String peersHome;
    private CountDownLatch latch;

    public Encoder(PipedInputStream rawData, PipedOutputStream encodedData,
            boolean mediaDebug, Logger logger, String peersHome,
            CountDownLatch latch) {
        this.rawData = rawData;
        this.encodedData = encodedData;
        this.mediaDebug = mediaDebug;
        this.logger = logger;
        this.peersHome = peersHome;
        this.latch = latch;
        isStopped = false;
    }
    
    public void run() {
        try {
            int buf_size = Capture.BUFFER_SIZE;
            byte[] buffer = new byte[buf_size];
            int numBytesRead;
            int tempBytesRead;
            if (mediaDebug) {
                SimpleDateFormat simpleDateFormat =
                        new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String date = simpleDateFormat.format(new Date());
                String dir = peersHome + File.separator
                        + AbstractSoundManager.MEDIA_DIR + File.separator;
                String fileName = dir + date + "_g711_encoder.output";
                try {
                    encoderOutput = new FileOutputStream(fileName);
                    fileName = dir + date + "_g711_encoder.input";
                    encoderInput = new FileOutputStream(fileName);
                } catch (FileNotFoundException e) {
                    logger.error("cannot create file", e);
                    return;
                }
            }
            //int ready;
            while (!isStopped || rawDataAvailable() > 0) {
                numBytesRead = 0;
                try {
                    while (numBytesRead < buf_size) {
                        // expect that the buffer is full
                        tempBytesRead = rawData.read(buffer, numBytesRead, buf_size - numBytesRead);
                        if (tempBytesRead < 0) {
                            setStopped(true);
                            break;
                        }
                        numBytesRead += tempBytesRead;
                    }
                    if (mediaDebug) {
                        try {
                            encoderInput.write(buffer, 0, numBytesRead);
                        } catch (IOException e) {
                            logger.error("cannot write to file", e);
                        }
                    }
                } catch (IOException e) {
                    // Getting an IOException reading from rawData is expected after the encoder has been stopped
                    if (!isStopped) logger.error("Error reading raw data", e);
                }

                byte[] encodedBuffer = process(buffer, numBytesRead);
                if (mediaDebug) {
                    try {
                        encoderOutput.write(encodedBuffer);
                    } catch (IOException e) {
                        logger.error("cannot write to file", e);
                    }
                }
                try {
                    encodedData.write(encodedBuffer);
                    encodedData.flush();
                } catch (IOException e) {
                    logger.error("Error writing encoded data", e);
                }
            }
        } finally {
            if (mediaDebug) {
                try {
                    encoderOutput.close();
                    encoderInput.close();
                } catch (IOException e) {
                    logger.error("cannot close file", e);
                }
            }
            try {
                rawData.close();
            } catch (IOException e) {
                logger.error("Error closing raw data input pipe", e);
            }
            try {
                encodedData.close();
            } catch (IOException e) {
                logger.error("Error closing encoded data output pipe", e);
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

    public abstract byte[] process(byte[] media, int len);

    private int rawDataAvailable() {
        try {
            return rawData.available();
        } catch (IOException e) {
            // PipedInputStream.available never throws IOException in practice
            logger.error("Error getting amount available raw data. Should never happen", e);
            return 0;
        }
    }

}
