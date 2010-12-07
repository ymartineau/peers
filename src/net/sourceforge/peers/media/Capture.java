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

import java.io.IOException;
import java.io.PipedOutputStream;

import net.sourceforge.peers.Logger;


public class Capture implements Runnable {
    
    public static final int SAMPLE_SIZE = 16;
    public static final int BUFFER_SIZE = SAMPLE_SIZE * 20;
    
    private PipedOutputStream rawData;
    private boolean isStopped;
    private SoundManager soundManager;
    private Logger logger;
    
    public Capture(PipedOutputStream rawData, SoundManager soundManager,
            Logger logger) {
        this.rawData = rawData;
        this.soundManager = soundManager;
        this.logger = logger;
        isStopped = false;
    }

    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];
        
        while (!isStopped) {
            int numBytesRead = soundManager.readData(buffer, 0, buffer.length);
            if (numBytesRead != buffer.length) {
                byte[] trimmed = new byte[numBytesRead];
                System.arraycopy(buffer, 0, trimmed, 0, numBytesRead);
                buffer = trimmed;
            }
            try {
                rawData.write(buffer, 0, numBytesRead);
            } catch (IOException e) {
                logger.error("input/output error", e);
                return;
            }
        }
    }

    public synchronized void setStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }

}
