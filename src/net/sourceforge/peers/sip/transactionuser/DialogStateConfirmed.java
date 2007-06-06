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


public class DialogStateConfirmed extends DialogState {

    public DialogStateConfirmed(String id, Dialog dialog) {
        super(id, dialog);
    }

    @Override
    public void receivedOrSentBye() {
        DialogState nextState = dialog.TERMINATED;
        dialog.setState(nextState);
        log(nextState);
        
    }
    
}
