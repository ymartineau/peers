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

package net.sourceforge.peers.javawebstart;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.media.AbstractSoundManager;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import java.net.SocketException;

public class EventManager implements SipListener {

    private UserAgent userAgent;
    private final RegisterSIPClient sipClient;
    private final Logger logger;

    public EventManager(RegisterSIPClient sipClient, String peersHome, Logger logger, AbstractSoundManager soundManager) {
        this.sipClient = sipClient;
        this.logger = logger;
        // create sip stack
        try {
            userAgent = new UserAgent(this, peersHome, logger, soundManager);
        } catch (SocketException e) {
            logger.error("Peers sip port unavailable, about to leave");
            System.exit(1);
        }
    }

    // sip events

    // never update gui from a non-swing thread, thus using
    // SwingUtilties.invokeLater for each event coming from sip stack.
    @Override
    public void registering(final SipRequest sipRequest) {
        sipClient.registering(sipRequest);
    }

    @Override
    public void registerFailed(final SipResponse sipResponse) {
        sipClient.registerFailed(sipResponse);
    }

    @Override
    public void incomingCall(SipRequest sipRequest, SipResponse provResponse) {
        // Not Implemented, only SIP Capabilities needed
    }

    @Override
    public void remoteHangup(SipRequest sipRequest) {
        // Not Implemented, only SIP Capabilities needed
    }

    @Override
    public void ringing(SipResponse sipResponse) {
        // Not Implemented, only SIP Capabilities needed
    }

    @Override
    public void calleePickup(SipResponse sipResponse) {
        // Not Implemented, only SIP Capabilities needed
    }

    @Override
    public void error(SipResponse sipResponse) {
        // Not Implemented, only SIP Capabilities needed
    }

    @Override
    public void registerSuccessful(final SipResponse sipResponse) {
        sipClient.registerSuccessful(sipResponse);
    }

    public void register() {
        if (userAgent == null) {
            // if several peers instances are launched concurrently,
            // display error message and exit
            return;
        }
        Config config = userAgent.getConfig();
        if (config.getPassword() != null) {
            try {
                userAgent.register();
            } catch (SipUriSyntaxException e) {
                logger.error(e.getMessage());
            }
        }
    }

}
