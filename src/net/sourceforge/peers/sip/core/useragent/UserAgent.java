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
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.Config;
import net.sourceforge.peers.sip.transaction.Transaction;
import net.sourceforge.peers.sip.transaction.TransactionManager;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.SipMessage;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;
import net.sourceforge.peers.sip.transport.TransportManager;

import org.dom4j.DocumentException;


public class UserAgent {

    public final static String CONFIG_FILE = "conf/peers.xml";
    
    private Config config;
    
    private List<String> peers;
    //private List<Dialog> dialogs;
    
    private CaptureRtpSender captureRtpSender;
    private IncomingRtpReader incomingRtpReader;
    
    private UAC uac;
    private UAS uas;

    private DialogManager dialogManager;
    private TransactionManager transactionManager;
    private TransportManager transportManager;

    public UserAgent() {
        
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
        
        Utils.setConfig(config);
        
        dialogManager = new DialogManager();
        transactionManager = new TransactionManager();
        transportManager = new TransportManager(transactionManager);
        transactionManager.setTransportManager(transportManager);
        
        uas = new UAS(this, dialogManager, transactionManager, transportManager);
        uac = new UAC(this, dialogManager, transactionManager, transportManager);
        
        peers = new ArrayList<String>();
        //dialogs = new ArrayList<Dialog>();

    }
    
    /**
     * Gives the sipMessage if sipMessage is a SipRequest or 
     * the SipRequest corresponding to the SipResponse
     * if sipMessage is a SipResponse
     * @param sipMessage
     * @return null if sipMessage is neither a SipRequest neither a SipResponse
     */
    public SipRequest getSipRequest(SipMessage sipMessage) {
        if (sipMessage instanceof SipRequest) {
            return (SipRequest) sipMessage;
        } else if (sipMessage instanceof SipResponse) {
            SipResponse sipResponse = (SipResponse) sipMessage;
            Transaction transaction = (Transaction)transactionManager
                .getClientTransaction(sipResponse);
            if (transaction == null) {
                transaction = (Transaction)transactionManager
                    .getServerTransaction(sipResponse);
            }
            if (transaction == null) {
                return null;
            }
            return transaction.getRequest();
        } else {
            return null;
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

    public UAS getUas() {
        return uas;
    }

    public UAC getUac() {
        return uac;
    }

    public DialogManager getDialogManager() {
        return dialogManager;
    }
    
}
