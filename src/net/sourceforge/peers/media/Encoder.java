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
    
    Copyright 2008, 2009, 2010 Yohann Martineau 
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

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.Utils;


public abstract class Encoder implements Runnable {
    
    private PipedInputStream rawData;
    private PipedOutputStream encodedData;
    private boolean isStopped;
    private FileOutputStream encoderOutput;
    private FileOutputStream encoderInput;
    private boolean mediaDebug;

    public Encoder(PipedInputStream rawData, PipedOutputStream encodedData,
            boolean mediaDebug) {
        this.rawData = rawData;
        this.encodedData = encodedData;
        this.mediaDebug = mediaDebug;
        isStopped = false;
    }
    
    public void run() {
        byte[] buffer = new byte[Capture.BUFFER_SIZE];
        if (mediaDebug) {
            SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String date = simpleDateFormat.format(new Date());
            String dir = Utils.getPeersHome() + File.separator
                + SoundManager.MEDIA_DIR + File.separator;
            String fileName = dir + date + "_g711_encoder.output";
            try {
                encoderOutput = new FileOutputStream(fileName);
                fileName = dir + date + "_g711_encoder.input";
                encoderInput = new FileOutputStream(fileName);
            } catch (FileNotFoundException e) {
                Logger.error("cannot create file", e);
                return;
            }
        }
        while (!isStopped) {
            int numBytesRead;
            try {
                numBytesRead = rawData.read(buffer, 0, buffer.length);
            } catch (IOException e) {
                Logger.error("input/output error", e);
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
                    encoderInput.write(trimmedBuffer);
                } catch (IOException e) {
                    Logger.error("cannot write to file", e);
                    break;
                }
            }
            byte[] ulawData = process(trimmedBuffer);
            if (mediaDebug) {
                try {
                    encoderOutput.write(ulawData);
                } catch (IOException e) {
                    Logger.error("cannot write to file", e);
                    break;
                }
            }
            try {
                encodedData.write(ulawData);
            } catch (IOException e) {
                Logger.error("input/output error", e);
                return;
            }
        }
        if (mediaDebug) {
            try {
                encoderOutput.close();
                encoderInput.close();
            } catch (IOException e) {
                Logger.error("cannot close file", e);
                return;
            }
        }
    }

    public synchronized void setStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }

    public abstract byte[] process(byte[] media);

}
