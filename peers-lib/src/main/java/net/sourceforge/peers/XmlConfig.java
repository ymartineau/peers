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

    Copyright 2010-2013 Yohann Martineau
*/

package net.sourceforge.peers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sourceforge.peers.media.MediaMode;
import net.sourceforge.peers.media.SoundSource;
import net.sourceforge.peers.sdp.Codec;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;


public class XmlConfig implements Config {

    public final static int RTP_DEFAULT_PORT = 8000;
    private final static String XML_CODEC_NODE = "codec";
    private final static String XML_CODEC_ATTR_NAME = "name";
    private final static String XML_CODEC_ATTR_PAYLOADTYPE = "payloadType";

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
    private String mediaFile;
    private SoundSource.DataFormat mediaFileDataFormat;
    private int rtpPort;
    private String authorizationUsername;
    private List<Codec> supportedCodecs;
    private String userAgentString;

    // corresponding DOM nodes

    private Node ipAddressNode;
    private Node userPartNode;
    private Node domainNode;
    private Node passwordNode;
    private Node outboundProxyNode;
    private Node sipPortNode;
    private Node mediaModeNode;
    private Node mediaDebugNode;
    private Node mediaFileDataFormatNode;
    private Node mediaFileNode;
    private Node rtpPortNode;
    private Node authUserNode;
    private Node supportedCodecsNode;

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
        ipAddressNode = getFirstChild(documentElement, "ipAddress");
        String address = ipAddressNode.getTextContent();
        try {
            if (isNullOrEmpty(ipAddressNode)) {
                localInetAddress = InetAddress.getLocalHost();
            } else {
                localInetAddress = InetAddress.getByName(address);
            }
        } catch (UnknownHostException e) {
            logger.error("unknown host: " + address, e);
        }
        userPartNode = getFirstChild(documentElement, "userPart");
        if (isNullOrEmpty(userPartNode)) {
            logger.error("userpart not found in configuration file");
        } else {
            userPart = userPartNode.getTextContent();
        }
        authUserNode = getFirstChild(documentElement, "authorizationUsername");
        if (! isNullOrEmpty(authUserNode)) {
            authorizationUsername = authUserNode.getTextContent();
        }
        domainNode = getFirstChild(documentElement, "domain");
        if (isNullOrEmpty(domainNode)) {
            logger.error("domain not found in configuration file");
        } else {
            domain = domainNode.getTextContent();
        }
        passwordNode = getFirstChild(documentElement, "password");
        if (!isNullOrEmpty(passwordNode)) {
            password = passwordNode.getTextContent();
        }
        outboundProxyNode = getFirstChild(documentElement, "outboundProxy");
        if (!isNullOrEmpty(outboundProxyNode)) {
            String uri = outboundProxyNode.getTextContent();
            try {
                outboundProxy = new SipURI(uri);
            } catch (SipUriSyntaxException e) {
                logger.error("sip uri syntax exception: " + uri, e);
            }
        }
        sipPortNode = getFirstChild(documentElement, "sipPort");
        if (isNullOrEmpty(sipPortNode)) {
            sipPort = RFC3261.TRANSPORT_DEFAULT_PORT;
        } else {
            sipPort = Integer.parseInt(sipPortNode.getTextContent());
        }
        mediaModeNode = getFirstChild(documentElement, "mediaMode");
        if (isNullOrEmpty(mediaModeNode)) {
            mediaMode = MediaMode.captureAndPlayback;
        } else {
            mediaMode = MediaMode.valueOf(mediaModeNode.getTextContent());
        }
        mediaDebugNode = getFirstChild(documentElement, "mediaDebug");
        if (isNullOrEmpty(mediaDebugNode)) {
            mediaDebug = false;
        } else {
            mediaDebug = Boolean.parseBoolean(mediaDebugNode.getTextContent());
        }
        mediaFileDataFormatNode = getFirstChild(documentElement, "mediaFileDataFormat");
        if (!isNullOrEmpty(mediaFileDataFormatNode)) {
            mediaFileDataFormat = SoundSource.DataFormat.fromShortAlias(mediaFileDataFormatNode.getTextContent());
        }
        mediaFileNode = getFirstChild(documentElement, "mediaFile");
        if (!isNullOrEmpty(mediaFileNode)) {
            mediaFile = mediaFileNode.getTextContent();
        }
        if (mediaMode == MediaMode.file) {
            if (mediaFile == null || "".equals(mediaFile.trim())) {
                logger.error("streaming from file but no file provided");
            }
        }
        rtpPortNode = getFirstChild(documentElement, "rtpPort");
        if (isNullOrEmpty(rtpPortNode)) {
            rtpPort = RTP_DEFAULT_PORT;
        } else {
            rtpPort = Integer.parseInt(rtpPortNode.getTextContent());
            if (rtpPort % 2 != 0) {
                logger.error("rtp port provided is " + rtpPort
                        + " rtp port must be even");
            }
        }

        supportedCodecs = new ArrayList<Codec>();
        supportedCodecsNode = getFirstChild(documentElement, "supportedCodecs");
        if(supportedCodecsNode != null && supportedCodecsNode.hasChildNodes())
        {
            NodeList nodeList = supportedCodecsNode.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                if (XML_CODEC_NODE.equals(node.getNodeName()) && node.hasAttributes()) {
                    Node name = node.getAttributes().getNamedItem(XML_CODEC_ATTR_NAME);
                    Node pt = node.getAttributes().getNamedItem(XML_CODEC_ATTR_PAYLOADTYPE);
                    Codec codec = new Codec();
                    codec.setName(name.getNodeValue());
                    codec.setPayloadType(Integer.parseInt(pt.getNodeValue()));
                    supportedCodecs.add(codec);
                }
            }
        }
    }

    private boolean isNullOrEmpty(Node node) {
        return node == null || "".equals(node.getTextContent().trim());
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
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
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
    public String getAuthorizationUsername() {
        return authorizationUsername;
    }

    @Override
    public List<Codec> getSupportedCodecs() {
        return supportedCodecs;
    }

    @Override
    public SoundSource.DataFormat getMediaFileDataFormat() { return mediaFileDataFormat; }

    @Override
    public String getMediaFile() {
        return mediaFile;
    }

    @Override
    public String getUserAgentString() {
        return userAgentString;
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

    @Override
    public void setAuthorizationUsername(String authorizationUsername) {
        this.authorizationUsername = authorizationUsername;
        authUserNode.setTextContent(authorizationUsername);
    }

    public void setUserAgentString(String uaString) {
        this.userAgentString = uaString;
    }

    @Override
    public void setSupportedCodecs(List<Codec> supportedCodecs) {
       this.supportedCodecs = supportedCodecs;

       // Remove all child nodes first
       if(supportedCodecsNode.hasChildNodes()) {
           NodeList nodeList = supportedCodecsNode.getChildNodes();
           for (int i = nodeList.getLength() - 1; i > 0; i--) {
               Node node = nodeList.item(i);
               supportedCodecsNode.removeChild(node);
           }
       }

       for(Codec codec : supportedCodecs)
       {
           Element node = document.createElement(XML_CODEC_NODE);
           node.setAttribute(XML_CODEC_ATTR_NAME, codec.getName());
           node.setAttribute(XML_CODEC_ATTR_PAYLOADTYPE, Integer.toString(codec.getPayloadType()));
           supportedCodecsNode.appendChild(node);
       }
    }

    @Override
    public void setMediaFileDataFormat(SoundSource.DataFormat mediaFileDataFormat) {
        this.mediaFileDataFormat = mediaFileDataFormat;
        mediaFileDataFormatNode.setTextContent(mediaFileDataFormat.getShortAlias());
    }

    @Override
    public void setMediaFile(String mediaFile) {
        this.mediaFile = mediaFile;
        mediaFileNode.setTextContent(mediaFile);
    }
}
