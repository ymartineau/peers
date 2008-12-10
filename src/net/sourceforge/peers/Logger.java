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
    
    Copyright 2007, 2008 Yohann Martineau 
*/

package net.sourceforge.peers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.peers.sip.Utils;

public class Logger {

    public final static String LOG_FILE =
        Utils.getPeersHome() + "logs" + File.separator + "peers.log";
    public final static String NETWORK_FILE =
        Utils.getPeersHome() + "logs" + File.separator + "transport.log";
    
    private static PrintWriter logWriter;
    private static PrintWriter networkWriter;
    private final static Object logMutex;
    private final static Object networkMutex;
    private final static SimpleDateFormat logFormatter;
    private final static SimpleDateFormat networkFormatter;
    
    static {
        try {
            logWriter = new PrintWriter(new BufferedWriter(
                    new FileWriter(LOG_FILE)));
            networkWriter = new PrintWriter(new BufferedWriter(
                    new FileWriter(NETWORK_FILE)));
        } catch (IOException e) {
            Logger.error("input/output error", e);
        }
        logMutex = new Object();
        networkMutex = new Object();
        logFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        networkFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    }
    
    public final static void debug(String message) {
        synchronized (logMutex) {
            logWriter.write(genericLog(message.toString(), "DEBUG"));
            logWriter.flush();
        }
    }
    
    public final static void info(String message) {
        synchronized (logMutex) {
            logWriter.write(genericLog(message.toString(), "INFO "));
            logWriter.flush();
        }
    }
    
    public final static void error(String message) {
        synchronized (logMutex) {
            logWriter.write(genericLog(message.toString(), "ERROR"));
            logWriter.flush();
        }
    }
    
    public final static void error(String message, Exception exception) {
        synchronized (logMutex) {
            logWriter.write(genericLog(message.toString(), "ERROR"));
            exception.printStackTrace(logWriter);
            logWriter.flush();
        }
    }
    
    private final static String genericLog(String message, String level) {
        StringBuffer buf = new StringBuffer();
        buf.append(logFormatter.format(new Date()));
        buf.append(" ");
        buf.append(level);
        buf.append(" [");
        buf.append(Thread.currentThread().getName());
        buf.append("] ");
        buf.append(message);
        buf.append("\n");
        return buf.toString();
    }
    
    public final static void traceNetwork(String message, String direction) {
        synchronized (networkMutex) {
            StringBuffer buf = new StringBuffer();
            buf.append(networkFormatter.format(new Date()));
            buf.append(" ");
            buf.append(direction);
            buf.append(" [");
            buf.append(Thread.currentThread().getName());
            buf.append("]\n\n");
            buf.append(message);
            buf.append("\n");
            networkWriter.write(buf.toString());
            networkWriter.flush();
        }
    }
    
}
