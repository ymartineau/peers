package net.sourceforge.peers.sip.transactionuser;

import java.io.ByteArrayInputStream;
import java.io.IOException;

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
        dialogManager = new DialogManager();
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
