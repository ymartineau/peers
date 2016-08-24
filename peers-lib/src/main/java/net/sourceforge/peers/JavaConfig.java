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
    
    Copyright 2012 Yohann Martineau 
*/

package net.sourceforge.peers;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.peers.media.MediaMode;
import net.sourceforge.peers.media.SoundSource;
import net.sourceforge.peers.rtp.RFC3551;
import net.sourceforge.peers.rtp.RFC4733;
import net.sourceforge.peers.sdp.Codec;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;

public class JavaConfig implements Config {

    private InetAddress localInetAddress;
    private InetAddress publicInetAddress;
    private String userPart;
    private String password;
    private String domain;
    private SipURI outboundProxy;
    private int sipPort;
    private MediaMode mediaMode;
    private boolean mediaDebug;
    private SoundSource.DataFormat mediaFileDataFormat;
    private String mediaFile;
    private int rtpPort;
    private String authorizationUsername;
    private List<Codec> supportedCodecs;

    public JavaConfig()
    {
        // Add default codecs
        supportedCodecs = new ArrayList<Codec>();

        Codec codec = new Codec();
        codec.setPayloadType(RFC3551.PAYLOAD_TYPE_PCMA);
        codec.setName(RFC3551.PCMA);
        supportedCodecs.add(codec);

        codec = new Codec();
        codec.setPayloadType(RFC3551.PAYLOAD_TYPE_PCMU);
        codec.setName(RFC3551.PCMU);
        supportedCodecs.add(codec);

        codec = new Codec();
        codec.setPayloadType(RFC4733.PAYLOAD_TYPE_TELEPHONE_EVENT);
        codec.setName(RFC4733.TELEPHONE_EVENT);
        //TODO add fmtp:101 0-15 attribute
        supportedCodecs.add(codec);
    }

    public JavaConfig(InetAddress localInetAddress, String userPart, String password, String domain,
                      SipURI outboundProxy, int sipPort, MediaMode mediaMode, boolean mediaDebug,
                      SoundSource.DataFormat mediaFileDataFormat, String mediaFile, int rtpPort, String authorizationUsername, List<Codec> supportedCodecs) {
        this.localInetAddress = localInetAddress;
        this.userPart = userPart;
        this.password = password;
        this.domain = domain;
        this.outboundProxy = outboundProxy;
        this.sipPort = sipPort;
        this.mediaMode = mediaMode;
        this.mediaDebug = mediaDebug;
        this.mediaFileDataFormat = mediaFileDataFormat;
        this.mediaFile = mediaFile;
        this.rtpPort = rtpPort;
        this.authorizationUsername = authorizationUsername;
        this.supportedCodecs = supportedCodecs;
    }

    @Override
    public void save() {
        throw new RuntimeException("not implemented");
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
    public void setLocalInetAddress(InetAddress inetAddress) {
        localInetAddress = inetAddress;
    }

    @Override
    public void setPublicInetAddress(InetAddress inetAddress) {
        publicInetAddress = inetAddress;
    }

    @Override
    public void setUserPart(String userPart) {
        this.userPart = userPart;
    }

    @Override
    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void setOutboundProxy(SipURI outboundProxy) {
        this.outboundProxy = outboundProxy;
    }

    @Override
    public void setSipPort(int sipPort) {
        this.sipPort = sipPort;
    }

    @Override
    public void setMediaMode(MediaMode mediaMode) {
        this.mediaMode = mediaMode;
    }

    @Override
    public void setMediaDebug(boolean mediaDebug) {
        this.mediaDebug = mediaDebug;
    }

    @Override
    public void setRtpPort(int rtpPort) {
        this.rtpPort = rtpPort;
    }

    public void setAuthorizationUsername(String authorizationUsername) {
        this.authorizationUsername = authorizationUsername;
    }

    @Override
    public void setSupportedCodecs(List<Codec> supportedCodecs) {
       this.supportedCodecs = supportedCodecs;
    }

    @Override
    public void setMediaFileDataFormat(SoundSource.DataFormat mediaFileDataFormat) {
        this.mediaFileDataFormat = mediaFileDataFormat;
    }

    @Override
    public void setMediaFile(String mediaFile) {
        this.mediaFile = mediaFile;
    }

}
