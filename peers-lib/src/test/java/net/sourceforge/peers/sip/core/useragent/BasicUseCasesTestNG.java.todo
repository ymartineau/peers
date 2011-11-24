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

package net.sourceforge.peers.sip.core.useragent;

import java.net.InetAddress;
import java.net.SocketException;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


public class BasicUseCasesTestNG {

    private UserAgent testUser1;
    private UserAgent testUser2;

    private UserSipListener user1SipListener;
    private UserSipListener user2SipListener;

    @BeforeTest
    public void init() throws SocketException, InterruptedException {
        String prefix = "resources/test-user-";
        String testUserHome = prefix + "1";
        Logger logger = new Logger(testUserHome);
        
        user1SipListener = new UserSipListener();
        testUser1 = new UserAgent(user1SipListener, testUserHome, logger);

        testUserHome = prefix + "2";
        logger = new Logger(testUserHome);
        user2SipListener = new UserSipListener();
        testUser2 = new UserAgent(user2SipListener, testUserHome, logger);
    }

    @Test(timeOut = 3000)
    public void uacCancel() throws SipUriSyntaxException, InterruptedException {
        Config config = testUser2.getConfig();
        InetAddress inetAddress = config.getLocalInetAddress();
        String host = inetAddress.getHostAddress();
        int port = config.getSipPort();
        UAC uac1 = testUser1.getUac();
        SipRequest invite =uac1.invite("sip:" + host + ":" + port,
                "sdfjhskdjfh");
        while (!user2SipListener.incomingCallInvoked) {
            Thread.sleep(50);
        }
        uac1.terminate(invite);
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
        public void calleePickup(SipResponse sipResponse) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void error(SipResponse sipResponse) {
            invite487Received = true;
        }

        @Override
        public void incomingCall(SipRequest sipRequest, SipResponse provResponse) {
            incomingCallInvoked = true;
        }

        @Override
        public void registerFailed(SipResponse sipResponse) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void registerSuccessful(SipResponse sipResponse) {
            // TODO Auto-generated method stub
        }

        @Override
        public void registering(SipRequest sipRequest) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void remoteHangup(SipRequest sipRequest) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void ringing(SipResponse sipResponse) {
            // TODO Auto-generated method stub
            
        }
        
    }

}
