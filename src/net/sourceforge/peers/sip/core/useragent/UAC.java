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

package net.sourceforge.peers.sip.core.useragent;

import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogStateConfirmed;
import net.sourceforge.peers.sip.transactionuser.DialogStateEarly;

public class UAC {

    private static UAC INSTANCE;
    
    public static UAC getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UAC();
        }
        return INSTANCE;
    }
    
    private InitialRequestManager initialRequestManager;
    private MidDialogRequestManager midDialogRequestManager;
    private String profileUri;
    
    private UAC() {
        initialRequestManager = new InitialRequestManager();
        midDialogRequestManager = new MidDialogRequestManager();
        profileUri = "sip:alice@atlanta.com";
    }
    
    public void invite(String requestUri) throws SipUriSyntaxException {
        //TODO make profileUri configurable
        initialRequestManager.createInitialRequest(requestUri,
                RFC3261.METHOD_INVITE, profileUri);
        
    }

    public void terminate(Dialog dialog) {
        if (dialog.getState() instanceof DialogStateEarly) {
            //TODO generate cancel
        } else if (dialog.getState() instanceof DialogStateConfirmed) {
            midDialogRequestManager.generateMidDialogRequest(
                    dialog, RFC3261.METHOD_BYE);
            
        }
        UserAgent.getInstance().getCaptureRtpSender().stop();
        UserAgent.getInstance().setCaptureRtpSender(null);
        UserAgent.getInstance().getIncomingRtpReader().stop();
        UserAgent.getInstance().setIncomingRtpReader(null);
    }
    
    public String getProfileUri() {
        return profileUri;
    }
}
