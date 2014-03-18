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
    
    Copyright 2010, 2012 Yohann Martineau 
*/

package net.sourceforge.peers.sip.core.useragent;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.JavaConfig;
import net.sourceforge.peers.media.AbstractSoundManager;
import net.sourceforge.peers.media.MediaMode;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


public class CancelTestNG {

    private UserAgent testUser1;
    private UserAgent testUser2;

    private UserSipListener user1SipListener;
    private UserSipListener user2SipListener;

    @BeforeTest
    public void init() throws SocketException, InterruptedException,
            UnknownHostException{

        Config config = new JavaConfig();
        config.setLocalInetAddress(InetAddress.getLocalHost());
        config.setMediaMode(MediaMode.none);
        user1SipListener = new UserSipListener();
        AbstractSoundManager soundManager = new DummySoundManager();
        testUser1 = new UserAgent(user1SipListener, config, null,
                soundManager);

        config = new JavaConfig();
        config.setLocalInetAddress(InetAddress.getLocalHost());
        config.setMediaMode(MediaMode.none);
        user2SipListener = new UserSipListener();
        testUser2 = new UserAgent(user2SipListener, config, null,
                soundManager);

    }

    @Test(timeOut = 3000)
    public void uacCancel() throws SipUriSyntaxException, InterruptedException {
        Config config = testUser2.getConfig();
        InetAddress inetAddress = config.getLocalInetAddress();
        String host = inetAddress.getHostAddress();
        int port = testUser2.getTransportManager().getSipPort();
        SipRequest invite = testUser1.invite("sip:" + host + ":" + port,
                "sdfjhskdjfh");
        while (!user2SipListener.incomingCallInvoked) {
            Thread.sleep(50);
        }
        testUser1.terminate(invite);
        while (!user1SipListener.invite487Received) {
            Thread.sleep(50);
        }
    }

    @AfterTest
    public void terminate() {
        testUser1.close();
        testUser2.close();
    }

    class UserSipListener implements SipListener {

        private boolean incomingCallInvoked;
        private boolean invite487Received;

        public UserSipListener() {
            incomingCallInvoked = false;
            invite487Received = false;
        }

        @Override
        public void calleePickup(SipResponse sipResponse) { }

        @Override
        public void error(SipResponse sipResponse) {
            invite487Received = true;
        }

        @Override
        public void incomingCall(SipRequest sipRequest, SipResponse provResponse) {
            incomingCallInvoked = true;
        }

        @Override
        public void registerFailed(SipResponse sipResponse) { }

        @Override
        public void registerSuccessful(SipResponse sipResponse) { }

        @Override
        public void registering(SipRequest sipRequest) { }

        @Override
        public void remoteHangup(SipRequest sipRequest) { }

        @Override
        public void ringing(SipResponse sipResponse) { }
        
    }

}
