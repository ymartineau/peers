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
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sourceforge.peers.media.MediaMode;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class XmlConfig implements Config {

    public final static int RTP_DEFAULT_PORT = 8000;

    private Logger logger;

    private File file;
    private Document document;

    // persistent variables

    private InetAddress localInetAddress;
    private String userPart;
    private String domain;
    private String password;
    private SipURI outboundProxy;
    private int sipPort;
    private MediaMode mediaMode;
    private boolean mediaDebug;
    private int rtpPort;
    
    // corresponding DOM nodes
    
    private Node ipAddressNode;
    private Node userPartNode;
    private Node domainNode;
    private Node passwordNode;
    private Node outboundProxyNode;
    private Node sipPortNode;
    private Node mediaModeNode;
    private Node mediaDebugNode;
    private Node rtpPortNode;

    // non-persistent variables

    private InetAddress publicInetAddress;

    //private InetAddress
    public XmlConfig(String fileName, Logger logger) {
        file = new File(fileName);
        this.logger = logger;
        if (!file.exists()) {
            logger.debug("config file " + fileName + " not found");
            return;
        }
        DocumentBuilderFactory documentBuilderFactory =
            DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            logger.error("parser configuration exception", e);
            return;
        }
        try {
            document = documentBuilder.parse(file);
        } catch (SAXException e) {
            logger.error("cannot parse " + fileName,e );
            return;
        } catch (IOException e) {
            logger.error("IOException", e);
            return;
        }
        Element documentElement = document.getDocumentElement();
        Node node = getFirstChild(documentElement, "network");
        node = getFirstChild(node, "interfaces");
        node = getFirstChild(node, "interface");
        node = getFirstChild(node, "address");
        ipAddressNode = node;
        if (node != null && !"".equals(node.getTextContent().trim())) {
            String address = node.getTextContent();
            try {
                localInetAddress = InetAddress.getByName(address);
            } catch (UnknownHostException e) {
                logger.error("unknown host: " + address, e);
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
                            this.localInetAddress = inetAddress;
                            found = true;
                        }
                    }
                }
            } catch (SocketException e) {
                logger.error("socket exception", e);
            }
            if (localInetAddress == null) {
                logger.error("IP address not found, configure it manually");
            }
        }
        node = getFirstChild(documentElement, "sip");
        Node parent = getFirstChild(node, "profile");
        node = getFirstChild(parent, "userpart");
        userPartNode = node;
        if (node != null && !"".equals(node.getTextContent().trim())) {
            userPart = node.getTextContent();
        } else {
            logger.error("userpart not found in configuration file");
        }
        node = getFirstChild(parent, "domain");
        domainNode = node;
        if (node != null && !"".equals(node.getTextContent().trim())) {
            domain = node.getTextContent();
        } else {
            logger.error("domain not found in configuration file");
        }
        node = getFirstChild(parent, "password");
        passwordNode = node;
        if (node != null && !"".equals(node.getTextContent().trim())) {
            password = node.getTextContent();
        }
        node = getFirstChild(parent, "outboundProxy");
        outboundProxyNode = node;
        if (node != null && !"".equals(node.getTextContent().trim())) {
            String uri = node.getTextContent();
            try {
                outboundProxy = new SipURI(uri);
            } catch (SipUriSyntaxException e) {
                logger.error("sip uri syntax exception: " + uri, e);
            }
        }
        node = getFirstChild(parent, "port");
        sipPortNode = node;
        if (node != null && !"".equals(node.getTextContent().trim())) {
            sipPort = Integer.parseInt(node.getTextContent());
        } else {
            sipPort = RFC3261.TRANSPORT_DEFAULT_PORT;
        }
        parent = getFirstChild(documentElement, "codecs");
        node = getFirstChild(parent, "mediaMode");
        mediaModeNode = node;
        if (node != null && !"".equals(node.getTextContent().trim())) {
            mediaMode = MediaMode.valueOf(node.getTextContent());
        } else {
            mediaMode = MediaMode.captureAndPlayback;
        }
        node = getFirstChild(parent, "mediaDebug");
        mediaDebugNode = node;
        if (node != null && !"".equals(node.getTextContent().trim())) {
            mediaDebug = Boolean.parseBoolean(node.getTextContent());
        } else {
            mediaDebug = false;
        }
        node = getFirstChild(documentElement, "rtp");
        node = getFirstChild(node, "port");
        rtpPortNode = node;
        if (node != null && !"".equals(node.getTextContent().trim())) {
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

    @Override
    public void save() {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            logger.error("cannot create transformer", e);
            return;
        }
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(file);
        } catch (IOException e) {
            logger.error("cannot create file writer", e);
            return;
        }
        StreamResult streamResult = new StreamResult(fileWriter);
        DOMSource domSource = new DOMSource(document);
        try {
            transformer.transform(domSource, streamResult);
        } catch (TransformerException e) {
            logger.error("cannot save config file", e);
            return;
        }
        logger.debug("config file saved");
    }

    @Override
    public InetAddress getLocalInetAddress() {
        return localInetAddress;
    }

    @Override
    public InetAddress getPublicInetAddress() {
        return publicInetAddress;
    }

    @Override
    public String getUserPart() {
        return userPart;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public SipURI getOutboundProxy() {
        return outboundProxy;
    }

    @Override
    public int getSipPort() {
        return sipPort;
    }

    @Override
    public MediaMode getMediaMode() {
        return mediaMode;
    }

    @Override
    public boolean isMediaDebug() {
        return mediaDebug;
    }

    @Override
    public int getRtpPort() {
        return rtpPort;
    }

    @Override
    public void setLocalInetAddress(InetAddress inetAddress) {
        this.localInetAddress = inetAddress;
        ipAddressNode.setTextContent(inetAddress.getHostAddress());
    }

    @Override
    public void setPublicInetAddress(InetAddress inetAddress) {
        this.publicInetAddress = inetAddress;
    }

    @Override
    public void setUserPart(String userPart) {
        this.userPart = userPart;
        userPartNode.setTextContent(userPart);
    }

    @Override
    public void setDomain(String domain) {
        this.domain = domain;
        domainNode.setTextContent(domain);
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
        passwordNode.setTextContent(password);
    }

    @Override
    public void setOutboundProxy(SipURI outboundProxy) {
        this.outboundProxy = outboundProxy;
        if (outboundProxy == null) {
            outboundProxyNode.setTextContent("");
        } else {
            outboundProxyNode.setTextContent(outboundProxy.toString());
        }
        
    }

    @Override
    public void setSipPort(int sipPort) {
        this.sipPort = sipPort;
        sipPortNode.setTextContent(Integer.toString(sipPort));
    }

    @Override
    public void setMediaMode(MediaMode mediaMode) {
        this.mediaMode = mediaMode;
        mediaModeNode.setTextContent(mediaMode.toString());
    }

    @Override
    public void setMediaDebug(boolean mediaDebug) {
        this.mediaDebug = mediaDebug;
        mediaDebugNode.setTextContent(Boolean.toString(mediaDebug));
    }

    @Override
    public void setRtpPort(int rtpPort) {
        this.rtpPort = rtpPort;
        rtpPortNode.setTextContent(Integer.toString(rtpPort));
    }

}
