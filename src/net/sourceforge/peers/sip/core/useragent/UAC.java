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
    
    Copyright 2007, 2008 Yohann Martineau 
*/

package net.sourceforge.peers.sip.core.useragent;

import net.sourceforge.peers.media.CaptureRtpSender;
import net.sourceforge.peers.media.IncomingRtpReader;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.handlers.InviteHandler;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transaction.TransactionManager;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transactionuser.DialogStateConfirmed;
import net.sourceforge.peers.sip.transactionuser.DialogStateEarly;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.TransportManager;

public class UAC {
    
    private InitialRequestManager initialRequestManager;
    private MidDialogRequestManager midDialogRequestManager;

    private String registerCallID;
    private String profileUri;
    
    //FIXME
    private UserAgent userAgent;
    private DialogManager dialogManager;
    
    /**
     * should be instanciated only once, it was a singleton.
     */
    public UAC(UserAgent userAgent,
            InitialRequestManager initialRequestManager,
            MidDialogRequestManager midDialogRequestManager,
            DialogManager dialogManager,
            TransactionManager transactionManager,
            TransportManager transportManager) {
        this.userAgent = userAgent;
        this.initialRequestManager = initialRequestManager;
        this.midDialogRequestManager = midDialogRequestManager;
        this.dialogManager = dialogManager;
        this.profileUri = RFC3261.SIP_SCHEME + RFC3261.SCHEME_SEPARATOR
            + userAgent.getUserpart() + RFC3261.AT + userAgent.getDomain();
        registerCallID = Utils.generateCallID(userAgent.getMyAddress());
    }

    /**
     * For the moment we consider that only one profile uri is used at a time.
     * @throws SipUriSyntaxException 
     */
    public void register() throws SipUriSyntaxException {
        String requestUri = RFC3261.SIP_SCHEME + RFC3261.SCHEME_SEPARATOR
            + userAgent.getDomain();
        initialRequestManager.createInitialRequest(requestUri,
                RFC3261.METHOD_REGISTER, profileUri, registerCallID);
    }
    
    public void unregister() {
        initialRequestManager.registerHandler.unregister();
    }
    
    public void invite(String requestUri, String callId)
            throws SipUriSyntaxException {
        initialRequestManager.createInitialRequest(requestUri,
                RFC3261.METHOD_INVITE, profileUri, callId);
        
    }
    
    public void terminate(Dialog dialog) {
        terminate(dialog, null);
    }
    
    public void terminate(Dialog dialog, SipRequest sipRequest) {
        if (dialog != null) {
            if (dialog.getState() instanceof DialogStateEarly) {
                //TODO generate cancel
                initialRequestManager.createCancel(sipRequest,
                        midDialogRequestManager, profileUri);
            } else if (dialog.getState() instanceof DialogStateConfirmed) {
                midDialogRequestManager.generateMidDialogRequest(
                        dialog, RFC3261.METHOD_BYE);
                
            }
            dialogManager.removeDialog(dialog.getId());
        }
        CaptureRtpSender captureRtpSender = userAgent.getCaptureRtpSender();
        if (captureRtpSender != null) {
            captureRtpSender.stop();
            userAgent.setCaptureRtpSender(null);
        }
        IncomingRtpReader incomingRtpReader = userAgent.getIncomingRtpReader();
        if (incomingRtpReader != null) {
            incomingRtpReader.stop();
            userAgent.setIncomingRtpReader(null);
        }
    }

    public InviteHandler getInviteHandler() {
        return initialRequestManager.getInviteHandler();
    }

}
