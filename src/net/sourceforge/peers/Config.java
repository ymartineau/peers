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
    
    Copyright 2010 Yohann Martineau 
*/

package net.sourceforge.peers;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.peers.media.MediaMode;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class Config {

    public final static int RTP_DEFAULT_PORT = 8000;

    private InetAddress inetAddress;
    private MediaMode mediaMode;
    private String userPart;
    private String domain;
    private String password;
    private SipURI outboundProxy;
    private int sipPort;
    private int rtpPort;

    public Config(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            Logger.debug("config file " + fileName + " not found");
            return;
        }
        DocumentBuilderFactory documentBuilderFactory =
            DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            Logger.error("parser configuration exception", e);
            return;
        }
        Document document;
        try {
            document = documentBuilder.parse(file);
        } catch (SAXException e) {
            Logger.error("cannot parse " + fileName,e );
            return;
        } catch (IOException e) {
            Logger.error("IOException", e);
            return;
        }
        Element documentElement = document.getDocumentElement();
        Node node = getFirstChild(documentElement, "network");
        node = getFirstChild(node, "interfaces");
        node = getFirstChild(node, "interface");
        node = getFirstChild(node, "address");
        if (node != null) {
            String address = node.getTextContent();
            try {
                inetAddress = InetAddress.getByName(address);
            } catch (UnknownHostException e) {
                Logger.error("unknown host: " + address, e);
            }
        } else {
            try {
                boolean found = false;
                Enumeration<NetworkInterface> e = NetworkInterface
                        .getNetworkInterfaces();
                while (e.hasMoreElements() && !found) {
                    NetworkInterface networkInterface = e.nextElement();
                    Enumeration<InetAddress> f = networkInterface
                            .getInetAddresses();
                    while (f.hasMoreElements() && !found) {
                        InetAddress inetAddress = f.nextElement();
                        if (inetAddress.isSiteLocalAddress()) {
                            this.inetAddress = inetAddress;
                            found = true;
                        }
                    }
                }
            } catch (SocketException e) {
                Logger.error("socket exception", e);
            }
            if (inetAddress == null) {
                Logger.error("IP address not found, configure it manually");
            }
        }
        node = getFirstChild(documentElement, "devices");
        node = getFirstChild(node, "mediaMode");
        if (node != null) {
            mediaMode = MediaMode.valueOf(node.getTextContent());
        } else {
            mediaMode = MediaMode.captureAndPlayback;
        }
        node = getFirstChild(documentElement, "sip");
        Node parent = getFirstChild(node, "profile");
        node = getFirstChild(parent, "userpart");
        if (node != null) {
            userPart = node.getTextContent();
        } else {
            Logger.error("userpart not found in configuration file");
        }
        node = getFirstChild(parent, "domain");
        if (node != null) {
            domain = node.getTextContent();
        } else {
            Logger.error("domain not found in configuration file");
        }
        node = getFirstChild(parent, "password");
        if (node != null) {
            password = node.getTextContent();
        }
        node = getFirstChild(parent, "outboundProxy");
        if (node != null) {
            String uri = node.getTextContent();
            try {
                outboundProxy = new SipURI(uri);
            } catch (SipUriSyntaxException e) {
                Logger.error("sip uri syntax exception: " + uri, e);
            }
        }
        node = getFirstChild(parent, "port");
        if (node != null) {
            sipPort = Integer.parseInt(node.getTextContent());
        } else {
            sipPort = RFC3261.TRANSPORT_DEFAULT_PORT;
        }
        node = getFirstChild(documentElement, "rtp");
        node = getFirstChild(node, "port");
        if (node != null) {
            rtpPort = Integer.parseInt(node.getTextContent());
        } else {
            rtpPort = RTP_DEFAULT_PORT;
        }
    }

    private Node getFirstChild(Node parent, String childName) {
        if (parent == null || childName == null) {
            return null;
        }
        NodeList nodeList = parent.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Node node = nodeList.item(i);
            if (childName.equals(node.getNodeName())) {
                return node;
            }
        }
        return null;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public MediaMode getMediaMode() {
        return mediaMode;
    }

    public String getUserPart() {
        return userPart;
    }

    public String getDomain() {
        return domain;
    }

    public String getPassword() {
        return password;
    }

    public SipURI getOutboundProxy() {
        return outboundProxy;
    }

    public int getSipPort() {
        return sipPort;
    }

    public int getRtpPort() {
        return rtpPort;
    }

}
