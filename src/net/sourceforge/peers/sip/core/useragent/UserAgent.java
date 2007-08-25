/*
    This file is part of Peers.

    Peers is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Peers is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    
    Copyright 2007 Yohann Martineau 
*/

package net.sourceforge.peers.sip.core.useragent;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.peers.media.CaptureRtpSender;
import net.sourceforge.peers.sip.transactionuser.Dialog;


public class UserAgent {

    private static UserAgent INSTANCE;
    
    public static UserAgent getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UserAgent();
        }
        return INSTANCE;
    }
    
    private List<String> peers;
    private List<Dialog> dialogs;
    
    private CaptureRtpSender captureRtpSender;

    private UserAgent() {
        peers = new ArrayList<String>();
        dialogs = new ArrayList<Dialog>();
    }
    
    public List<Dialog> getDialogs() {
        return dialogs;
    }

    public List<String> getPeers() {
        return peers;
    }

    public Dialog getDialog(String peer) {
        for (Dialog dialog : dialogs) {
            String remoteUri = dialog.getRemoteUri();
            if (remoteUri != null) {
                if (remoteUri.contains(peer)) {
                    return dialog;
                }
            }
        }
        return null;
    }

    public CaptureRtpSender getCaptureRtpSender() {
        return captureRtpSender;
    }

    public void setCaptureRtpSender(CaptureRtpSender captureRtpSender) {
        this.captureRtpSender = captureRtpSender;
    }
    
    

}
