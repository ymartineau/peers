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

import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transactionuser.Dialog;

public class UACTestMain {

    public static void main(String[] args) {
        String requestUri = "sip:bob@" + Utils.getInstance()
            .getMyAddress().getHostAddress() + ":6060";
        UserAgent userAgent;
        try {
            userAgent = new UserAgent();
            userAgent.getUac().invite(requestUri,
                    Utils.getInstance().generateCallID());
        } catch (SipUriSyntaxException e) {
            e.printStackTrace();
            return;
        }
        
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        Dialog dialog = userAgent.getDialogManager().getDialog(requestUri);
        if (dialog != null) {
            userAgent.getUac().terminate(dialog);
        } else {
            System.err.println("dialog not found");
        }
    }
}
