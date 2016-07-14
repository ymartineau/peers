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
    
    Copyright 2012 Yohann Martineau 
*/
package net.sourceforge.peers.media;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.sourceforge.peers.Logger;

// To create an audio file for peers, you can use audacity:
//
// Edit > Preferences
//
// - Peripherals
//   - Channels: 1 (Mono)
// - Quality
//   - Sampling frequency of default: 8000 Hz
//   - Default Sample Format: 16 bits
//
// Validate
//
// Record audio
//
// File > Export
//
// - File type: AIFF (Apple) signed 16-bit PCM, File name: test.raw
// - or, File type: Other uncompressed files, Header: RAW (header-less), Encoding: A-law, File name: test.alaw
//
// Validate

public class FileReader implements SoundSource {

    public final static int BUFFER_SIZE = 256;

    private Object finishedSync = new Object();
    private FileInputStream fileInputStream;
    private DataFormat fileDataFormat;
    private Logger logger;

    public FileReader(String fileName, DataFormat fileDataFormat, Logger logger) {
        this.logger = logger;
        try {
            fileInputStream = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            logger.error("file not found: " + fileName, e);
        }
        this.fileDataFormat = fileDataFormat;
    }

    public synchronized void close() {
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                logger.error("io exception", e);
            }
            fileInputStream = null;
            synchronized (finishedSync) {
                finishedSync.notifyAll();
            }
        }
    }

    @Override
    public DataFormat dataProduced() {
        return (fileDataFormat != null)?fileDataFormat:DataFormat.DEFAULT;
    }

    @Override
    public synchronized byte[] readData() {
        if (fileInputStream == null) {
            return null;
        }
        byte buffer[] = new byte[BUFFER_SIZE];
        try {
            int read;
            if ((read = fileInputStream.read(buffer)) >= 0) {
                // TODO There is a problem if not the entire buffer was filled. That is not communicated to the reader of the returned byte-array
                if (read < buffer.length) System.out.println("Buffer was not completely filled, but we are sending it all through anyway");
                return buffer;
            } else {
                close();
            }
        } catch (IOException e) {
            logger.error("io exception", e);
        }
        return null;
    }

    @Override
    public boolean finished() {
        return fileInputStream == null;
    }

    @Override
    public void waitFinished() throws InterruptedException {
        if (!finished()) {
            synchronized (finishedSync) {
                while (!finished()) {
                    finishedSync.wait();
                }
            }
        }
        return;
    }
}
