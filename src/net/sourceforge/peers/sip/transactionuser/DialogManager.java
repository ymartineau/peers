/*
    This file is part of Peers.

    Peers is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Peers is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    
    Copyright 2007 Yohann Martineau 
*/

package net.sourceforge.peers.sip.transactionuser;

import java.util.Hashtable;

import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderParamName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.transport.SipMessage;
import net.sourceforge.peers.sip.transport.SipResponse;


public class DialogManager {

    private static DialogManager INSTANCE;
    
    public static DialogManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DialogManager();
        }
        return INSTANCE;
    }
    
    private Hashtable<String, Dialog> dialogs;
    
    private DialogManager() {
        dialogs = new Hashtable<String, Dialog>();
    }

    public Dialog createDialog(SipResponse sipResponse) {
        SipHeaders sipHeaders = sipResponse.getSipHeaders();
        String callID = sipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_CALLID)).toString();
        SipHeaderFieldValue from = sipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_FROM));
        SipHeaderFieldValue to = sipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_TO));
        String fromTag = from.getParam(new SipHeaderParamName(RFC3261.PARAM_TAG));
        String toTag = to.getParam(new SipHeaderParamName(RFC3261.PARAM_TAG));
        Dialog dialog;
        if (sipHeaders.get(new SipHeaderFieldName(RFC3261.HDR_VIA)) == null) {
            //createDialog is called from UAS side, in layer Transaction User
            dialog = new Dialog(callID, toTag, fromTag);
        } else {
            //createDialog is called from UAC side, in syntax encoding layer
            dialog = new Dialog(callID, fromTag, toTag);
        }
        dialogs.put(dialog.getId(), dialog);
        return dialog;
    }
    
    public Dialog getDialog(SipMessage sipMessage) {
        SipHeaders sipHeaders = sipMessage.getSipHeaders();
        String callID = sipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_CALLID)).toString();
        SipHeaderFieldValue from = sipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_FROM));
        SipHeaderFieldValue to = sipHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_TO));
        String fromTag = from.getParam(new SipHeaderParamName(RFC3261.PARAM_TAG));
        String toTag = to.getParam(new SipHeaderParamName(RFC3261.PARAM_TAG));
        Dialog dialog = dialogs.get(getDialogId(callID, fromTag, toTag));
        if (dialog != null) {
            return dialog;
        }
        return dialogs.get(getDialogId(callID, toTag, fromTag));
    }
    
    private String getDialogId(String callID, String localTag, String remoteTag) {
        StringBuffer buf = new StringBuffer();
        buf.append(callID);
        buf.append(Dialog.ID_SEPARATOR);
        buf.append(localTag);
        buf.append(Dialog.ID_SEPARATOR);
        buf.append(remoteTag);
        return buf.toString();
    }
}
