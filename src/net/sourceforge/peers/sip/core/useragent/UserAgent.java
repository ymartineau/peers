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

package net.sourceforge.peers.sip.core.useragent;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.peers.media.CaptureRtpSender;
import net.sourceforge.peers.media.IncomingRtpReader;
import net.sourceforge.peers.sip.core.Config;

import org.dom4j.DocumentException;


public class UserAgent {

    public final static String CONFIG_FILE = "conf/peers.xml";
    
    private static UserAgent INSTANCE;
    
    public static UserAgent getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UserAgent();
        }
        return INSTANCE;
    }
    
    private Config config;
    
    private List<String> peers;
    //private List<Dialog> dialogs;
    
    private CaptureRtpSender captureRtpSender;
    private IncomingRtpReader incomingRtpReader;

    private UserAgent() {
        peers = new ArrayList<String>();
        //dialogs = new ArrayList<Dialog>();
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            System.err.println("configuration file not found: " + CONFIG_FILE);
            System.exit(-1);
        }
        try {
            config = new Config(configFile.toURI().toURL());
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    
//    public List<Dialog> getDialogs() {
//        return dialogs;
//    }

    public List<String> getPeers() {
        return peers;
    }

//    public Dialog getDialog(String peer) {
//        for (Dialog dialog : dialogs) {
//            String remoteUri = dialog.getRemoteUri();
//            if (remoteUri != null) {
//                if (remoteUri.contains(peer)) {
//                    return dialog;
//                }
//            }
//        }
//        return null;
//    }

    public CaptureRtpSender getCaptureRtpSender() {
        return captureRtpSender;
    }

    public void setCaptureRtpSender(CaptureRtpSender captureRtpSender) {
        this.captureRtpSender = captureRtpSender;
    }

    public IncomingRtpReader getIncomingRtpReader() {
        return incomingRtpReader;
    }

    public void setIncomingRtpReader(IncomingRtpReader incomingRtpReader) {
        this.incomingRtpReader = incomingRtpReader;
    }

    public synchronized Config getConfig() {
        return config;
    }
    
}
