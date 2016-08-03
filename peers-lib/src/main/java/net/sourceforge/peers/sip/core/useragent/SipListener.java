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
    
    Copyright 2007, 2008, 2009, 2010 Yohann Martineau 
*/


package net.sourceforge.peers.sip.core.useragent;

import net.sourceforge.peers.rtp.RFC4733;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

public interface SipListener {

    void registering(SipRequest sipRequest);

    void registerSuccessful(SipResponse sipResponse);

    void registerFailed(SipResponse sipResponse);

    void incomingCall(SipRequest sipRequest, SipResponse provResponse);

    void remoteHangup(SipRequest sipRequest);

    void ringing(SipResponse sipResponse);

    void calleePickup(SipResponse sipResponse);

    void error(SipResponse sipResponse);

    void dtmfEvent(RFC4733.DTMFEvent dtmfEvent, int duration);

}
