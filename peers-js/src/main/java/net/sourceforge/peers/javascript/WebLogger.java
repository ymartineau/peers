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
    
    Copyright 2013 Yohann Martineau 
*/

package net.sourceforge.peers.javascript;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.peers.Logger;

public class WebLogger implements Logger {

    private WebLoggerOutput out;
    private SimpleDateFormat logFormatter;
    private SimpleDateFormat networkFormatter;

    public WebLogger(WebLoggerOutput out) {
        this.out = out;
        logFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        networkFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    }

    @Override
    public synchronized void debug(String message) {
        System.out.println(genericLog(message.toString(), "DEBUG"));
        out.javaLog(genericLog(message.toString(), "DEBUG"));
    }

    @Override
    public synchronized void info(String message) {
        System.out.println(genericLog(message.toString(), "INFO"));
        out.javaLog(genericLog(message.toString(), "INFO"));
    }

    @Override
    public synchronized void error(String message) {
        System.err.println(genericLog(message.toString(), "ERROR"));
        out.javaLog(genericLog(message.toString(), "ERROR"));
    }

    @Override
    public synchronized void error(String message, Exception exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        stringWriter.write(genericLog(message, "ERROR"));
        stringWriter.flush();
        exception.printStackTrace(printWriter);
        printWriter.flush();
        System.err.println(stringWriter.toString());
        out.javaLog(stringWriter.toString());
    }

    @Override
    public synchronized void traceNetwork(String message, String direction) {
        StringBuffer buf = new StringBuffer();
        buf.append(networkFormatter.format(new Date()));
        buf.append(" ");
        buf.append(direction);
        buf.append(" [");
        buf.append(Thread.currentThread().getName());
        buf.append("]\n\n");
        buf.append(message);
        buf.append("\n");
        System.out.println(buf.toString());
        out.javaNetworkLog(buf.toString());
    }

    private final String genericLog(String message, String level) {
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

}
