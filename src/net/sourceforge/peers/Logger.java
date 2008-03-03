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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    public static final String LOG_FILE = "logs/peers.log";
    public static final String NETWORK_FILE = "logs/transport.log";
    
    private static Logger INSTANCE;
    
    public static Logger getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Logger();
        }
        return INSTANCE;
    }
    
    private PrintWriter logWriter;
    private PrintWriter networkWriter;
    private Object logMutex;
    private Object networkMutex;
    private SimpleDateFormat logFormatter;
    private SimpleDateFormat networkFormatter;
    
    private Logger() {
        try {
            logWriter = new PrintWriter(new BufferedWriter(
                    new FileWriter(LOG_FILE)));
            networkWriter = new PrintWriter(new BufferedWriter(
                    new FileWriter(NETWORK_FILE)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        logMutex = new Object();
        networkMutex = new Object();
        logFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        networkFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    }
    
    public void debug(Object message) {
        synchronized (logMutex) {
            logWriter.write(genericLog(message.toString(), "DEBUG"));
            logWriter.flush();
        }
    }
    
    public void info(Object message) {
        synchronized (logMutex) {
            logWriter.write(genericLog(message.toString(), "INFO "));
            logWriter.flush();
        }
    }
    
    public void error(Object message) {
        synchronized (logMutex) {
            logWriter.write(genericLog(message.toString(), "WARN "));
            logWriter.flush();
        }
    }
    
    private String genericLog(String message, String level) {
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
    
    public void traceNetwork(String message, String direction) {
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
