/*
    This file is part of Peers.

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
    
    Copyright 2008 Yohann Martineau 
*/

package net.sourceforge.peers.media;

import java.io.IOException;
import java.io.PipedOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import net.sourceforge.peers.Logger;


public class Capture implements Runnable {
    
    public static final int SAMPLE_SIZE = 16;
    public static final int BUFFER_SIZE = SAMPLE_SIZE * 20;
    
    private PipedOutputStream rawData;
    private boolean isStopped;
    
    public Capture(PipedOutputStream rawData) {
        this.rawData = rawData;
        isStopped = false;
    }
    
    public void run() {
        AudioFormat format = new AudioFormat(8000, SAMPLE_SIZE, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine line;
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
        } catch (LineUnavailableException e) {
            Logger.error("line unavailable", e);
            return;
        }
        line.start();
        byte[] buffer = new byte[BUFFER_SIZE];
        
        while (!isStopped) {
            int numBytesRead = line.read(buffer, 0, buffer.length);
//            byte[] trimmedBuffer;
//            if (numBytesRead < buffer.length) {
//                trimmedBuffer = new byte[numBytesRead];
//                System.arraycopy(buffer, 0, trimmedBuffer, 0, numBytesRead);
//            } else {
//                trimmedBuffer = buffer;
//            }
            try {
                rawData.write(buffer, 0, numBytesRead);
            } catch (IOException e) {
                Logger.error("input/output error", e);
                return;
            }
        }
        line.close();
    }

    public synchronized void setStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }

}
