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
    
    Copyright 2008, 2009, 2010 Yohann Martineau 
*/

package net.sourceforge.peers.sip.transactionuser;

import net.sourceforge.peers.FileLogger;

import org.testng.annotations.Test;

public class DialogTestNG {
    
    @Test
    public void initialState() {
        Dialog dialog = new Dialog("", "", "", new FileLogger(null));
        assert dialog.getState() instanceof DialogStateInit;
    }

    //INIT
    
    @Test
    public void transitionInitEarly() {
        Dialog dialog = new Dialog("", "", "", new FileLogger(null));
        dialog.receivedOrSent1xx();
        assert dialog.getState() instanceof DialogStateEarly;
    }

    @Test
    public void transitionInitConfirmed() {
        Dialog dialog = new Dialog("", "", "", new FileLogger(null));
        dialog.receivedOrSent2xx();
        assert dialog.getState() instanceof DialogStateConfirmed;
    }
    
    @Test
    public void transitionInitTerminated() {
        Dialog dialog = new Dialog("", "", "", new FileLogger(null));
        dialog.receivedOrSent300To699();
        assert dialog.getState() instanceof DialogStateTerminated;
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldThrowIfByeOnInit() {
        Dialog dialog = new Dialog("", "", "", new FileLogger(null));
        dialog.receivedOrSentBye();
    }

    //EARLY
    
    @Test
    public void transitionEarlyEarly(){
        Dialog dialog = new Dialog("", "", "", new FileLogger(null));
        dialog.receivedOrSent1xx();
        dialog.receivedOrSent1xx();
        assert dialog.getState() instanceof DialogStateEarly;
    }
    
    @Test
    public void transitionEarlyConfirmed() {
        Dialog dialog = new Dialog("", "", "", new FileLogger(null));
        dialog.receivedOrSent1xx();
        dialog.receivedOrSent2xx();
        assert dialog.getState() instanceof DialogStateConfirmed;
    }
    
    @Test
    public void transitionEarlyTerminated() {
        Dialog dialog = new Dialog("", "", "", new FileLogger(null));
        dialog.receivedOrSent1xx();
        dialog.receivedOrSent300To699();
        assert dialog.getState() instanceof DialogStateTerminated;
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldThrowIfByeOnEarly() {
        Dialog dialog = new Dialog("", "", "", new FileLogger(null));
        dialog.receivedOrSent1xx();
        dialog.receivedOrSentBye();
    }
    
    //CONFIRMED
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldThrowIf1xxOnConfirmed() {
        Dialog dialog = new Dialog("", "", "", new FileLogger(null));
        dialog.receivedOrSent2xx();
        dialog.receivedOrSent1xx();
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldThrowIf2xxOnConfirmed() {
        Dialog dialog = new Dialog("", "", "", new FileLogger(null));
        dialog.receivedOrSent2xx();
        dialog.receivedOrSent2xx();
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldThrowIfErrOnConfirmed() {
        Dialog dialog = new Dialog("", "", "", new FileLogger(null));
        dialog.receivedOrSent2xx();
        dialog.receivedOrSent300To699();
    }
    
    @Test
    public void transitionConfirmedTerminated() {
        Dialog dialog = new Dialog("", "", "", new FileLogger(null));
        dialog.receivedOrSent2xx();
        dialog.receivedOrSentBye();
        assert dialog.getState() instanceof DialogStateTerminated;
    }
    
    //TERMINATED
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldThrowIf1xxOnTerminated() {
        Dialog dialog = new Dialog("", "", "", new FileLogger(null));
        dialog.receivedOrSent2xx();
        dialog.receivedOrSentBye();
        dialog.receivedOrSent1xx();
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldThrowIf2xxOnTerminated() {
        Dialog dialog = new Dialog("", "", "", new FileLogger(null));
        dialog.receivedOrSent2xx();
        dialog.receivedOrSentBye();
        dialog.receivedOrSent2xx();
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldThrowIfErrOnTerminated() {
        Dialog dialog = new Dialog("", "", "", new FileLogger(null));
        dialog.receivedOrSent2xx();
        dialog.receivedOrSentBye();
        dialog.receivedOrSent300To699();
    }
    
    // retransmissions can be sent to dialog
//    @Test(expectedExceptions = IllegalStateException.class)
//    public void shouldThrowIfByeOnTerminated() {
//        Dialog dialog = new Dialog("", "", "", new FileLogger(null));
//        dialog.receivedOrSent2xx();
//        dialog.receivedOrSentBye();
//        dialog.receivedOrSentBye();
//    }
    
}
