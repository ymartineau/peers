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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import net.sourceforge.peers.Logger;

public class Encoder implements Runnable {

    private PipedInputStream rawData;
    private PipedOutputStream encodedData;
    private boolean isStopped;
    
    public Encoder(PipedInputStream rawData, PipedOutputStream encodedData) {
        this.rawData = rawData;
        this.encodedData = encodedData;
        isStopped = false;
    }
    
    public void run() {
        byte[] buffer = new byte[Capture.BUFFER_SIZE];
        
        // for the moment, we consider that the number of
        // bytes read corresponds to the buffer size
        byte[] ulawData = new byte[Capture.BUFFER_SIZE/2];
        
        while (!isStopped) {

            try {
                rawData.read(buffer, 0, Capture.BUFFER_SIZE);
            } catch (IOException e) {
                Logger.error("input/output error", e);
                return;
            }
//            System.out.println(buffer);
            
            // encode data
            
            AudioUlawEncodeDecode02.value = 0;
            AudioUlawEncodeDecode02.increment = 1;
            AudioUlawEncodeDecode02.limit = 4;
            
            for (int i = 0; i < Capture.BUFFER_SIZE; i += 2) {
                //TODO data length odd
                //TODO manage data endianess
                short value = (short)buffer[i];
                value += 256 * (short)buffer[i+1];
                ulawData[i/2] = AudioUlawEncodeDecode02.encode(value);
            }
            
            try {
                encodedData.write(ulawData);
            } catch (IOException e) {
                Logger.error("input/output error", e);
                return;
            }
        }
    }
    
    public synchronized void setStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }

}
