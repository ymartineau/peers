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
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.media.CaptureRtpSender;
import net.sourceforge.peers.media.IncomingRtpReader;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.core.Config;
import net.sourceforge.peers.sip.core.useragent.handlers.ByeHandler;
import net.sourceforge.peers.sip.core.useragent.handlers.CancelHandler;
import net.sourceforge.peers.sip.core.useragent.handlers.InviteHandler;
import net.sourceforge.peers.sip.core.useragent.handlers.OptionsHandler;
import net.sourceforge.peers.sip.core.useragent.handlers.RegisterHandler;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transaction.Transaction;
import net.sourceforge.peers.sip.transaction.TransactionManager;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.SipMessage;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;
import net.sourceforge.peers.sip.transport.TransportManager;

import org.dom4j.DocumentException;
import org.dom4j.Node;


public class UserAgent {

    public final static String CONFIG_FILE = "conf/peers.xml";
    
    private Config config;
    
    private List<String> peers;
    //private List<Dialog> dialogs;
    
    private CaptureRtpSender captureRtpSender;
    private IncomingRtpReader incomingRtpReader;
    
    private UAC uac;
    private UAS uas;

    private ChallengeManager challengeManager;
    
    private DialogManager dialogManager;
    private TransactionManager transactionManager;
    private TransportManager transportManager;

    private InetAddress myAddress;
    private int sipPort;
    private int rtpPort;
    private int cseqCounter;
    
    private String userpart;
    private String domain;
    
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
        
        //config
        
        //find stack ip address
        Node node = config.selectSingleNode("//peers:address");
        if (node == null) {
            //automatically detect stack ip address
            try {
                boolean found = false;
                Enumeration<NetworkInterface> e = NetworkInterface
                        .getNetworkInterfaces();
                while (e.hasMoreElements() && !found) {
                    NetworkInterface networkInterface = e.nextElement();
//                    Logger.getInstance().debug(networkInterface.getDisplayName());
                    Enumeration<InetAddress> f = networkInterface
                            .getInetAddresses();
                    while (f.hasMoreElements() && !found) {
                        InetAddress inetAddress = f.nextElement();
                        if (inetAddress.isSiteLocalAddress()) {
                            this.myAddress = inetAddress;
                            found = true;
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
            if (myAddress == null) {
                throw new RuntimeException("ip address cannot be determined " +
                        "please configure it manually in " + CONFIG_FILE);
            }
        } else {
            //manually configured stack ip address
            try {
                myAddress = InetAddress.getByName(node.getText());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        //stack sip listening port
        node = config.selectSingleNode("//peers:sip/peers:profile/peers:port");
        if (node == null) {
            sipPort = RFC3261.TRANSPORT_DEFAULT_PORT;
        } else {
            sipPort = Integer.parseInt(node.getText());
        }
        
        //stack sip user part
        node = config.selectSingleNode("//peers:sip/peers:profile/peers:userpart");
        if (node == null) {
            userpart = "alice";
        } else {
            userpart = node.getText();
        }
        
        //stack sip domain
        node = config.selectSingleNode("//peers:sip/peers:profile/peers:domain");
        if (node == null) {
            domain = "atlanta.com";
        } else {
            domain = node.getText();
        }
        
        //stack sip password
        node = config.selectSingleNode("//peers:sip/peers:profile/peers:password");
        String password = null;
        if (node != null) {
            password = node.getText();
        }
        
        //stack rtp listening port
        node = config.selectSingleNode("//peers:rtp/peers:port");
        rtpPort = Integer.parseInt(node.getText());
        cseqCounter = 0;
        
        StringBuffer buf = new StringBuffer();
        buf.append("starting user agent [");
        buf.append("myAddress: ").append(myAddress.getHostAddress()).append(", ");
        buf.append("sipPort: ").append(sipPort).append(", ");
        buf.append("userpart: ").append(userpart).append(", ");
        buf.append("domain: ").append(domain).append("]");
        Logger.info(buf);
        
        //transaction user
        
        dialogManager = new DialogManager();
        
        //transaction
        
        transactionManager = new TransactionManager();
        
        //transport
        
        transportManager = new TransportManager(transactionManager,
                myAddress,
                sipPort);
        
        transactionManager.setTransportManager(transportManager);
        
        //core
        
        InviteHandler inviteHandler = new InviteHandler(this,
                dialogManager,
                transactionManager,
                transportManager);
        CancelHandler cancelHandler = new CancelHandler(this,
                dialogManager,
                transactionManager,
                transportManager);
        ByeHandler byeHandler = new ByeHandler(this,
                dialogManager,
                transactionManager,
                transportManager);
        OptionsHandler optionsHandler = new OptionsHandler(transactionManager,
                transportManager);
        RegisterHandler registerHandler = new RegisterHandler(
                transactionManager,
                transportManager);
        
        InitialRequestManager initialRequestManager =
            new InitialRequestManager(
                this,
                inviteHandler,
                cancelHandler,
                byeHandler,
                optionsHandler,
                registerHandler,
                dialogManager,
                transactionManager,
                transportManager);
        MidDialogRequestManager midDialogRequestManager =
            new MidDialogRequestManager(
                this,
                inviteHandler,
                cancelHandler,
                byeHandler,
                optionsHandler,
                registerHandler,
                dialogManager,
                transactionManager,
                transportManager);
        
        uas = new UAS(this,
                initialRequestManager,
                midDialogRequestManager,
                dialogManager,
                transactionManager,
                transportManager);
        String profileUri = RFC3261.SIP_SCHEME + RFC3261.SCHEME_SEPARATOR
            + userpart + RFC3261.AT + domain;
        uac = new UAC(this,
                profileUri,
                initialRequestManager,
                midDialogRequestManager,
                dialogManager,
                transactionManager,
                transportManager);
        
        if (password != null) {
            challengeManager = new ChallengeManager(userpart,
                    password,
                    initialRequestManager,
                    profileUri);
            registerHandler.setChallengeManager(challengeManager);
        }

        peers = new ArrayList<String>();
        //dialogs = new ArrayList<Dialog>();

        if (password != null) {
            try {
                uac.register();
            } catch (SipUriSyntaxException e) {
                e.printStackTrace();
            }
        }
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

    public String generateCSeq(String method) {
        StringBuffer buf = new StringBuffer();
        buf.append(cseqCounter++);
        buf.append(' ');
        buf.append(method);
        return buf.toString();
    }
    
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
    
    public InetAddress getMyAddress() {
        return myAddress;
    }
    
    public int getSipPort() {
        return sipPort;
    }

    public int getRtpPort() {
        return rtpPort;
    }

    public String getDomain() {
        return domain;
    }

    public String getUserpart() {
        return userpart;
    }
    
}
