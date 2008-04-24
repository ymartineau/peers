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

package net.sourceforge.peers.sip;

import static net.sourceforge.peers.sip.RFC3261.HDR_VIA;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import net.sourceforge.peers.sip.core.Config;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldMultiValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.transaction.Transaction;
import net.sourceforge.peers.sip.transaction.TransactionManager;
import net.sourceforge.peers.sip.transport.SipMessage;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.dom4j.Node;


public class Utils {

    //FIXME
    private static Config config;
    
    private static Utils INSTANCE;
    
    public static Utils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Utils();
        }
        return INSTANCE;
    }
    
    public static void setConfig(Config config) {
        Utils.config = config;
    }
    
    private InetAddress myAddress;
    private int sipPort;
    private int rtpPort;
    private int cseqCounter;

    private Utils() {
        super();
        
        Node node = config.selectSingleNode("//peers:address");
        if (node == null) {
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
        } else {
            try {
                myAddress = InetAddress.getByName(node.getText());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        node = config.selectSingleNode("//peers:sip/peers:profile/peers:port");
        if (node == null) {
            sipPort = RFC3261.TRANSPORT_DEFAULT_PORT;
        } else {
            sipPort = Integer.parseInt(node.getText());
        }
        
        node = config.selectSingleNode("//peers:rtp/peers:port");
        rtpPort = Integer.parseInt(node.getText());
        cseqCounter = 0;
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

    public SipHeaderFieldValue getTopVia(SipMessage sipMessage) {
        SipHeaders sipHeaders = sipMessage.getSipHeaders();
        SipHeaderFieldName viaName = new SipHeaderFieldName(HDR_VIA);
        SipHeaderFieldValue via = sipHeaders.get(viaName);
        if (via instanceof SipHeaderFieldMultiValue) {
            via = ((SipHeaderFieldMultiValue)via).getValues().get(0);
        }
        return via;
    }
    
    public String generateTag() {
        return randomString(8);
    }
    
    public String generateCallID() {
        //TODO make a hash using current time millis, public ip @, private @, and a random string
        StringBuffer buf = new StringBuffer();
        buf.append(randomString(8));
        buf.append('-');
        buf.append(String.valueOf(System.currentTimeMillis()));
        buf.append('@');
        buf.append(myAddress.getHostName());
        return buf.toString();
    }
    
    public String generateCSeq(String method) {
        StringBuffer buf = new StringBuffer();
        buf.append(cseqCounter++);
        buf.append(' ');
        buf.append(method);
        return buf.toString();
    }
    
    public String generateBranchId() {
        StringBuffer buf = new StringBuffer();
        buf.append(RFC3261.BRANCHID_MAGIC_COOKIE);
        //TODO must be unique across space and time...
        buf.append(randomString(9));
        return buf.toString();
    }
    
    public String getMessageCallId(SipMessage sipMessage) {
        SipHeaderFieldValue callId = sipMessage.getSipHeaders().get(
                new SipHeaderFieldName(RFC3261.HDR_CALLID));
        return callId.getValue();
    }
    
    public String randomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz" +
                       "ABCDEFGHIFKLMNOPRSTUVWXYZ" +
                       "0123456789";
        StringBuffer buf = new StringBuffer(length);
        for (int i = 0; i < length; ++i) {
            int pos = (int)Math.round(Math.random() * (chars.length() - 1));
            buf.append(chars.charAt(pos));
        }
        return buf.toString();
    }
    
    public void copyHeader(SipMessage src, SipMessage dst, String name) {
        SipHeaderFieldName sipHeaderFieldName = new SipHeaderFieldName(name);
        SipHeaderFieldValue sipHeaderFieldValue = src.getSipHeaders().get(sipHeaderFieldName);
        if (sipHeaderFieldValue != null) {
            dst.getSipHeaders().add(sipHeaderFieldName, sipHeaderFieldValue);
        }
    }
    
    /**
     * adds Max-Forwards Supported and Require headers
     * @param headers
     */
    public void addCommonHeaders(SipHeaders headers) {
        //Max-Forwards
        
        headers.add(new SipHeaderFieldName(RFC3261.HDR_MAXFORWARDS),
                new SipHeaderFieldValue(
                        String.valueOf(RFC3261.DEFAULT_MAXFORWARDS)));
        
        //TODO Supported and Require
    }
    
    
    public String getUserPart(String sipUri) {
        int start = sipUri.indexOf(RFC3261.SCHEME_SEPARATOR);
        int end = sipUri.indexOf(RFC3261.AT);
        return sipUri.substring(start + 1, end);
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
            Transaction transaction = (Transaction)TransactionManager
                .getInstance().getClientTransaction(sipResponse);
            if (transaction == null) {
                transaction = (Transaction)TransactionManager
                    .getInstance().getServerTransaction(sipResponse);
            }
            if (transaction == null) {
                return null;
            }
            return transaction.getRequest();
        } else {
            return null;
        }
    }
}
