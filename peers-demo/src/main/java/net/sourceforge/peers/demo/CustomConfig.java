package net.sourceforge.peers.demo;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.media.MediaMode;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;

public class CustomConfig implements Config {

    private InetAddress publicIpAddress;

    @Override
    public InetAddress getLocalInetAddress() {
        InetAddress inetAddress;
        try {
            // if you have only one active network interface, getLocalHost()
            // should be enough
            //inetAddress = InetAddress.getLocalHost();
            // if you have several network interfaces like I do,
            // select the right one after running ipconfig or ifconfig
            inetAddress = InetAddress.getByName("192.168.1.10");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
        return inetAddress;
    }

    @Override
    public InetAddress getPublicInetAddress() { return publicIpAddress; }
    @Override public String getUserPart() { return "alice"; }
    @Override public String getDomain() { return "atlanta.com"; }
    @Override public String getPassword() { return "secret1234"; }
    @Override
    public MediaMode getMediaMode() { return MediaMode.captureAndPlayback; }

    @Override public String getAuthorizationUsername() { return getUserPart(); }

    @Override
    public void setPublicInetAddress(InetAddress inetAddress) {
        publicIpAddress = inetAddress;
    }
    
    @Override public SipURI getOutboundProxy() { return null; }
    @Override public int getSipPort() { return 0; }
    @Override public boolean isMediaDebug() { return false; }
    @Override public String getMediaFile() { return null; }
    @Override public int getRtpPort() { return 0; }
    @Override public void setLocalInetAddress(InetAddress inetAddress) { }
    @Override public void setUserPart(String userPart) { }
    @Override public void setDomain(String domain) { }
    @Override public void setPassword(String password) { }
    @Override public void setOutboundProxy(SipURI outboundProxy) { }
    @Override public void setSipPort(int sipPort) { }
    @Override public void setMediaMode(MediaMode mediaMode) { }
    @Override public void setMediaDebug(boolean mediaDebug) { }
    @Override public void setMediaFile(String mediaFile) { }
    @Override public void setRtpPort(int rtpPort) { }
    @Override public void save() { }
    @Override public void setAuthorizationUsername(String authorizationUsername) { }
    
}
