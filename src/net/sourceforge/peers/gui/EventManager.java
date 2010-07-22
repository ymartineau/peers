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

package net.sourceforge.peers.gui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.SipMessage;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

public class EventManager implements SipListener, MainFrameListener,
        CallFrameListener {

    private UserAgent userAgent;
    private MainFrame mainFrame;
    private Map<String, CallFrame> callFrames;

    public EventManager(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        // create sip stack
        userAgent = new UserAgent();
        userAgent.setSipListener(EventManager.this);
        callFrames = Collections.synchronizedMap(
                new HashMap<String, CallFrame>());
    }

    // sip events

    @Override
    public synchronized void registerFailed(SipResponse sipResponse) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public synchronized void registerSuccessful(SipResponse sipResponse) {
        mainFrame.setLabelText("Account registered");
    }

    @Override
    public synchronized void calleePickup(SipResponse sipResponse) {
        CallFrame callFrame = getCallFrame(sipResponse);
        if (callFrame != null) {
            callFrame.calleePickup();
        }
    }

    @Override
    public synchronized void error(SipResponse sipResponse) {
        CallFrame callFrame = getCallFrame(sipResponse);
        if (callFrame != null) {
            callFrame.error(sipResponse);
        }
    }

    @Override
    public synchronized void incomingCall(final SipRequest sipRequest,
            SipResponse provResponse) {
        SipHeaders sipHeaders = sipRequest.getSipHeaders();
        SipHeaderFieldName sipHeaderFieldName =
            new SipHeaderFieldName(RFC3261.HDR_FROM);
        SipHeaderFieldValue from = sipHeaders.get(sipHeaderFieldName);
        final String fromValue = from.getValue();
        String callId = Utils.getMessageCallId(sipRequest);
        CallFrame callFrame = new CallFrame(fromValue, callId, this);
        callFrames.put(callId, callFrame);
        callFrame.setSipRequest(sipRequest);
        callFrame.incomingCall();
    }

    @Override
    public synchronized void remoteHangup(SipRequest sipRequest) {
        CallFrame callFrame = getCallFrame(sipRequest);
        if (callFrame != null) {
            callFrame.remoteHangup();
        }
    }

    @Override
    public synchronized void ringing(SipResponse sipResponse) {
        CallFrame callFrame = getCallFrame(sipResponse);
        if (callFrame != null) {
            callFrame.ringing();
        }
    }

    // main frame events

    @Override
    public synchronized void callClicked(String uri) {
        String callId = Utils.generateCallID(userAgent.getMyAddress());
        CallFrame callFrame = new CallFrame(uri, callId, this);
        callFrames.put(callId, callFrame);
        SipRequest sipRequest;
        try {
            sipRequest = userAgent.getUac().invite(uri, callId);
        } catch (SipUriSyntaxException e) {
            Logger.error("SipUriSyntaxException", e);
            mainFrame.setLabelText(e.getMessage());
            return;
        }
        callFrame.setSipRequest(sipRequest);
        callFrame.callClicked();
    }

    @Override
    public synchronized void windowClosed() {
        try {
            userAgent.getUac().unregister();
        } catch (Exception e) {
            Logger.error("error while unregistering", e);
        }
        //FIXME ugly exit
        System.exit(0);
    }

    // call frame events
    
    @Override
    public synchronized void hangupClicked(SipRequest sipRequest) {
        userAgent.getUac().terminate(sipRequest);
    }

    @Override
    public synchronized void pickupClicked(SipRequest sipRequest) {
        String callId = Utils.getMessageCallId(sipRequest);
        DialogManager dialogManager = userAgent.getDialogManager();
        Dialog dialog = dialogManager.getDialogFromCallId(callId);
        userAgent.getUas().acceptCall(sipRequest, dialog);
    }
    
    @Override
    public synchronized void busyHereClicked(SipRequest sipRequest) {
        userAgent.getUas().rejectCall(sipRequest);
    }

    private CallFrame getCallFrame(SipMessage sipMessage) {
        String callId = Utils.getMessageCallId(sipMessage);
        return callFrames.get(callId);
    }

}
