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

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.sourceforge.peers.media.MediaMode;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;

import org.testng.annotations.Test;

public class ConfigTestNG {

    public static final String TEST_CONFIG_FILE =
        "test/net/sourceforge/peers/configTest.xml";

    @Test
    public void testSave() throws SipUriSyntaxException, UnknownHostException {
        Config config = new Config(TEST_CONFIG_FILE);
        InetAddress localHost = InetAddress.getLocalHost();
        String userPart = "alice";
        String domain = "sourceforge.net";
        String password = "123";
        SipURI sipURI = new SipURI("sip:sourceforge.net;lr");
        int sipPort = 6060;
        MediaMode mediaMode = MediaMode.echo;
        boolean mediaDebug = true;
        int rtpPort = 8002;
        config.setInetAddress(localHost);
        config.setUserPart(userPart);
        config.setDomain(domain);
        config.setPassword(password);
        config.setOutboundProxy(sipURI);
        config.setSipPort(sipPort);
        config.setMediaMode(mediaMode);
        config.setMediaDebug(mediaDebug);
        config.setRtpPort(rtpPort);
        config.save();
        config = new Config(TEST_CONFIG_FILE);
        assert localHost.equals(config.getInetAddress());
        assert userPart.equals(config.getUserPart());
        assert domain.equals(config.getDomain());
        assert password.equals(config.getPassword());
        assert sipURI.toString().equals(config.getOutboundProxy().toString());
        assert sipPort == config.getSipPort();
        assert mediaMode == config.getMediaMode();
        assert mediaDebug == config.isMediaDebug();
        assert rtpPort == config.getRtpPort();
    }

}
