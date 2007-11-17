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

package net.sourceforge.peers.sip.core.useragent;

import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.UAC;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transactionuser.Dialog;

public class UACTestMain {

    public static void main(String[] args) {
        String requestUri = "sip:bob@" + Utils.getInstance()
            .getMyAddress().getHostAddress() + ":6060";
        try {
            UAS.getInstance();
            UAC.getInstance().invite(requestUri);
        } catch (SipUriSyntaxException e) {
            e.printStackTrace();
        }
        
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        Dialog dialog = UserAgent.getInstance().getDialog(requestUri);
        if (dialog != null) {
            UAC.getInstance().terminate(dialog);
        } else {
            System.err.println("dialog not found");
        }
    }
}
