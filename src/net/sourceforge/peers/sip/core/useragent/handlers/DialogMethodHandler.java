/*
    This file is part of Peers.

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
    
    Copyright 2007 Yohann Martineau 
*/

package net.sourceforge.peers.sip.core.useragent.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.NameAddress;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldMultiValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderParamName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.transaction.Transaction;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;


public abstract class DialogMethodHandler extends MethodHandler {


    
    protected Timer ackTimer;
    
    public DialogMethodHandler() {

        ackTimer = new Timer();
    }
    
    protected Dialog buildDialogForUas(SipResponse sipResponse,
            SipRequest sipRequest) {
        //12.1.1
        
        //prepare response
        
        SipHeaders reqHeaders = sipRequest.getSipHeaders();
        SipHeaders respHeaders = sipResponse.getSipHeaders();
        
          //copy record-route
        SipHeaderFieldName recordRouteName =
            new SipHeaderFieldName(RFC3261.HDR_RECORD_ROUTE);
        SipHeaderFieldValue reqRecRoute = reqHeaders.get(recordRouteName);
        if (reqRecRoute != null) {
        	respHeaders.add(recordRouteName, reqRecRoute);
        }
        
        SipHeaderFieldName contactName = new SipHeaderFieldName(RFC3261.HDR_CONTACT);
        
        Dialog dialog = DialogManager.getInstance().createDialog(sipResponse);
        
        //build dialog state
        
          //route set
        SipHeaderFieldValue recordRoute =
            respHeaders.get(new SipHeaderFieldName(RFC3261.HDR_RECORD_ROUTE));
        ArrayList<String> routeSet = new ArrayList<String>();
        if (recordRoute != null) {
            if (recordRoute instanceof SipHeaderFieldMultiValue) {
                SipHeaderFieldMultiValue multiRecordRoute =
                    (SipHeaderFieldMultiValue) recordRoute;
                for (SipHeaderFieldValue routeValue : multiRecordRoute.getValues()) {
                    routeSet.add(routeValue.getValue());
                }
            } else {
                routeSet.add(recordRoute.getValue());
            }
        }
        dialog.setRouteSet(routeSet);
        
          //remote target
        SipHeaderFieldValue reqContact = reqHeaders.get(contactName);
        String remoteTarget = reqContact.getValue();
        if (remoteTarget.indexOf(RFC3261.LEFT_ANGLE_BRACKET) > -1) {
            remoteTarget = NameAddress.nameAddressToUri(remoteTarget);
        }
        dialog.setRemoteTarget(remoteTarget);
        
          //remote cseq
        SipHeaderFieldName cseqName = new SipHeaderFieldName(RFC3261.HDR_CSEQ);
        SipHeaderFieldValue cseq = reqHeaders.get(cseqName);
        String remoteCseq = cseq.getValue().substring(0, cseq.getValue().indexOf(' '));
        dialog.setRemoteCSeq(Integer.parseInt(remoteCseq));
        
          //callid
        SipHeaderFieldName callidName = new SipHeaderFieldName(RFC3261.HDR_CALLID);
        SipHeaderFieldValue callid = reqHeaders.get(callidName);
        dialog.setCallId(callid.getValue());
        
          //local tag
        SipHeaderFieldName toName = new SipHeaderFieldName(RFC3261.HDR_TO);
        SipHeaderFieldValue to = respHeaders.get(toName);
        SipHeaderParamName tagName = new SipHeaderParamName(RFC3261.PARAM_TAG);
        String toTag = to.getParam(tagName);
        dialog.setLocalTag(toTag);
        
          //remote tag
        SipHeaderFieldName fromName = new SipHeaderFieldName(RFC3261.HDR_FROM);
        SipHeaderFieldValue from = reqHeaders.get(fromName);
        String fromTag = from.getParam(tagName);
        dialog.setRemoteTag(fromTag);
        
          //remote uri
        
        String remoteUri = from.getValue();
        if (remoteUri.indexOf(RFC3261.LEFT_ANGLE_BRACKET) > -1) {
            remoteUri = NameAddress.nameAddressToUri(remoteUri);
        }
        dialog.setRemoteUri(remoteUri);
        
          //local uri
        
        String localUri = to.getValue();
        if (localUri.indexOf(RFC3261.LEFT_ANGLE_BRACKET) > -1) {
            localUri = NameAddress.nameAddressToUri(localUri);
        }
        dialog.setLocalUri(localUri);
        
        return dialog;
    }
    
    protected Dialog buildDialogForUac(SipResponse sipResponse, Transaction transaction) {
        SipHeaders headers = sipResponse.getSipHeaders();
        SipHeaderFieldValue to = headers.get(new SipHeaderFieldName(RFC3261.HDR_TO));
        String toTag = to.getParam(new SipHeaderParamName(RFC3261.PARAM_TAG));
        DialogManager dialogManager = DialogManager.getInstance();
        
        Dialog dialog = dialogManager.getDialog(sipResponse);
        if (dialog == null) {
            dialog = dialogManager.createDialog(sipResponse);
        }
        
        //12.1.2
        
        //TODO if request uri contains sips scheme or if sent over tls => dialog.setSecure(true)
        
        //route set
        
        dialog.setRouteSet(computeRouteSet(headers));        
        
        //remote target
        
        SipHeaderFieldValue contact = headers.get(new SipHeaderFieldName(RFC3261.HDR_CONTACT));
        String remoteTarget = NameAddress.nameAddressToUri(contact.toString());
        dialog.setRemoteTarget(remoteTarget);
        
        //local cseq
        
        SipHeaders requestSipHeaders = transaction.getRequest().getSipHeaders();
        String requestCSeq = requestSipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_CSEQ)).toString();
        requestCSeq = requestCSeq.substring(0, requestCSeq.indexOf(' '));
        dialog.setLocalCSeq(Integer.parseInt(requestCSeq));
        
        //callID
        
        String requestCallID = requestSipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_CALLID)).toString();
        dialog.setCallId(requestCallID);
        
        //local tag
        
        SipHeaderFieldValue requestFrom = requestSipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_FROM));
        String requestFromTag =
            requestFrom.getParam(new SipHeaderParamName(RFC3261.PARAM_TAG));
        dialog.setLocalTag(requestFromTag);
        
        //remote tag
        
        dialog.setRemoteTag(toTag);
        
          //remote uri
        
        String remoteUri = to.getValue();
        if (remoteUri.indexOf(RFC3261.LEFT_ANGLE_BRACKET) > -1) {
            remoteUri = NameAddress.nameAddressToUri(remoteUri);
        }
        dialog.setRemoteUri(remoteUri);
        
          //local uri
        
        String localUri = requestFrom.getValue();
        if (localUri.indexOf(RFC3261.LEFT_ANGLE_BRACKET) > -1) {
            localUri = NameAddress.nameAddressToUri(localUri);
        }
        dialog.setLocalUri(localUri);
        
        return dialog;
    }

    protected ArrayList<String> computeRouteSet(SipHeaders headers) {
        SipHeaderFieldValue recordRoute =
            headers.get(new SipHeaderFieldName(RFC3261.HDR_RECORD_ROUTE));
        ArrayList<String> routeSet = new ArrayList<String>();
        if (recordRoute != null) {
            if (recordRoute instanceof SipHeaderFieldMultiValue) {
                ArrayList<SipHeaderFieldValue> values =
                    ((SipHeaderFieldMultiValue)recordRoute).getValues();
                for (SipHeaderFieldValue value : values) {
                    //reverse order
                    routeSet.add(0, value.toString());
                }
            } else {
                routeSet.add(recordRoute.toString());
            }
        }
        return routeSet;
    }
    
    //TODO see if AckHandler is usable
    class AckTimerTask extends TimerTask {

        private String toUri;
        
        public AckTimerTask(String toUri) {
            super();
            this.toUri = toUri;
        }

        @Override
        public void run() {
            ArrayList<Dialog> purgedDialogs = new ArrayList<Dialog>();
            List<Dialog> dialogs = UserAgent.getInstance().getDialogs();
            for (Dialog dialog : dialogs) {
                String remoteUri = dialog.getRemoteUri();
                if (remoteUri.equals(toUri) &&
                        !dialog.CONFIRMED.equals(dialog.getState())) {
                    dialog.receivedOrSentBye();
                    purgedDialogs.add(dialog);
                }
            }
            for (Dialog dialog : purgedDialogs) {
                dialogs.remove(dialog);
            }
        }
        
    }


}
