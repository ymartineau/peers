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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.sourceforge.peers.FileLogger;
import net.sourceforge.peers.sip.syntaxencoding.SipParser;
import net.sourceforge.peers.sip.syntaxencoding.SipParserException;
import net.sourceforge.peers.sip.transport.SipMessage;
import net.sourceforge.peers.sip.transport.SipResponse;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DialogManagerTestNG {

    private DialogManager dialogManager;
    
    @BeforeClass
    public void createDialogManager() {
        dialogManager = new DialogManager(new FileLogger(null));
    }
    
    @Test
    public void testCreateDialogForUAS() throws IOException, SipParserException {
        String message = "SIP/2.0 200 OK\r\n" +
                "From: sip:alice@atlanta.com;tag=abc\r\n" +
                "To: Bob <sip:bob@biloxi.com>;tag=345\r\n" +
                "Call-ID: lijr345lkj3@somehost\r\n" +
                "\r\n";
        SipResponse sipResponse = (SipResponse)parse(message);
        Dialog dialog = dialogManager.createDialog(sipResponse);
        assert dialog != null;
        assert "lijr345lkj3@somehost".equals(dialog.getCallId());
        assert "abc".equals(dialog.getRemoteTag());
        assert "345".equals(dialog.getLocalTag());
    }
    
    @Test
    public void testCreateDialogForUAC() throws IOException, SipParserException {
        String message = "SIP/2.0 200 OK\r\n" +
                "From: sip:alice@atlanta.com;tag=abc\r\n" +
                "To: Bob <sip:bob@biloxi.com>;tag=345\r\n" +
                "Call-ID: lijr345lkj3@somehost\r\n" +
                "Via: SIP/2.0/UDP 192.2.4.2;branch=23456SG/\r\n" +
                "\r\n";
        SipResponse sipResponse = (SipResponse)parse(message);
        Dialog dialog = dialogManager.createDialog(sipResponse);
        assert dialog != null;
        assert "lijr345lkj3@somehost".equals(dialog.getCallId());
        assert "345".equals(dialog.getRemoteTag());
        assert "abc".equals(dialog.getLocalTag());
    }
    
    private SipMessage parse(String message) throws IOException, SipParserException {
        ByteArrayInputStream bais = new ByteArrayInputStream(message.getBytes());
        SipParser sipParser = new SipParser();
        SipMessage sipMessage = sipParser.parse(bais);
        return sipMessage;
    }
}
