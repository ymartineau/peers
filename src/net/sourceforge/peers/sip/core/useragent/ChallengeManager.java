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
    
    Copyright 2008 Yohann Martineau 
*/

package net.sourceforge.peers.sip.core.useragent;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sip.RFC2617;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transport.SipMessage;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

public class ChallengeManager implements MessageInterceptor {

    public static final String ALGORITHM_MD5 = "MD5";
    
    public static String md5hash(String message) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(ALGORITHM_MD5);
        } catch (NoSuchAlgorithmException e) {
            Logger.error("no such algorithm " + ALGORITHM_MD5, e);
            return null;
        }
        byte[] messageBytes = message.getBytes();
        byte[] messageMd5 = messageDigest.digest(messageBytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(out);
//        StringBuffer buf = new StringBuffer();
        for (byte b : messageMd5) {
            int u_b = (b < 0) ? 256 + b : b;
            printStream.printf("%02x", u_b);
//            buf.append(Integer.toHexString(u_b));
        }
        return out.toString();
//        System.out.println();
//        return buf.toString();
    }
    
    public static void main(String[] args) {
        System.out.println("md5(a) = " + md5hash("a"));
    }
    
    private String username;
    private String password;
    private String realm;
    private String nonce;
    private String requestUri;
    private String digest;
    private String profileUri;
    
    private InitialRequestManager initialRequestManager;
    
    public ChallengeManager(String username, String password,
            InitialRequestManager initialRequestManager,
            String profileUri) {
        super();
        this.username = username;
        this.password = password;
        this.initialRequestManager = initialRequestManager;
        this.profileUri = profileUri;
    }

    public void handleChallenge(SipRequest sipRequest,
            SipResponse sipResponse) {
        SipHeaders responseHeaders = sipResponse.getSipHeaders();
        SipHeaderFieldValue wwwAuthenticate =
            responseHeaders.get(new SipHeaderFieldName(
                    RFC3261.HDR_WWW_AUTHENTICATE));
        if (wwwAuthenticate == null) {
            return;
        }
        if (!wwwAuthenticate.getValue().startsWith(RFC2617.SCHEME_DIGEST)) {
            Logger.info("unsupported challenge scheme in header: "
                    + wwwAuthenticate);
            return;
        }
        String headerValue = wwwAuthenticate.getValue();
        realm = getParameter(RFC2617.PARAM_REALM, headerValue);
        nonce = getParameter(RFC2617.PARAM_NONCE, headerValue);
        String method = sipRequest.getMethod();
        requestUri = sipRequest.getRequestUri().toString();
        digest = getRequestDigest(method);
        // FIXME message should be copied "as is" not created anew from scratch
        // and this technique is not clean
        String callId = responseHeaders.get(
                new SipHeaderFieldName(RFC3261.HDR_CALLID)).getValue();
        try {
            initialRequestManager.createInitialRequest(
                    requestUri, method, profileUri, callId, this);
        } catch (SipUriSyntaxException e) {
            Logger.error("syntax error", e);
        }
    }
    
    private String getRequestDigest(String method) {
        StringBuffer buf = new StringBuffer();
        buf.append(username);
        buf.append(RFC2617.DIGEST_SEPARATOR);
        buf.append(realm);
        buf.append(RFC2617.DIGEST_SEPARATOR);
        buf.append(password);
        String ha1 = md5hash(buf.toString());
        buf = new StringBuffer();
        buf.append(method);
        buf.append(RFC2617.DIGEST_SEPARATOR);
        buf.append(requestUri);
        String ha2 = md5hash(buf.toString());
        buf = new StringBuffer();
        buf.append(ha1);
        buf.append(RFC2617.DIGEST_SEPARATOR);
        buf.append(nonce);
        buf.append(RFC2617.DIGEST_SEPARATOR);
        buf.append(ha2);
        return md5hash(buf.toString());
    }
    
    private String getParameter(String paramName, String header) {
        int paramPos = header.indexOf(paramName);
        if (paramPos < 0) {
            return null;
        }
        int paramNameLength = paramName.length();
        if (paramPos + paramNameLength + 3 > header.length()) {
            Logger.info("Malformed " + RFC3261.HDR_WWW_AUTHENTICATE + " header");
            return null;
        }
        if (header.charAt(paramPos + paramNameLength) !=
                    RFC2617.PARAM_VALUE_SEPARATOR) {
            Logger.info("Malformed " + RFC3261.HDR_WWW_AUTHENTICATE + " header");
            return null;
        }
        if (header.charAt(paramPos + paramNameLength + 1) !=
                    RFC2617.PARAM_VALUE_DELIMITER) {
            Logger.info("Malformed " + RFC3261.HDR_WWW_AUTHENTICATE + " header");
            return null;
        }
        header = header.substring(paramPos + paramNameLength + 2);
        int endDelimiter = header.indexOf(RFC2617.PARAM_VALUE_DELIMITER);
        if (endDelimiter < 0) {
            Logger.info("Malformed " + RFC3261.HDR_WWW_AUTHENTICATE + " header");
            return null;
        }
        return header.substring(0, endDelimiter);
    }

    public void postProcess(SipMessage sipMessage) {
        if (realm == null || nonce == null || digest == null) {
            return;
        }
        SipHeaders sipHeaders = sipMessage.getSipHeaders();
        StringBuffer buf = new StringBuffer();
        buf.append(RFC2617.SCHEME_DIGEST).append(" ");
        appendParameter(buf, RFC2617.PARAM_USERNAME, username);
        buf.append(RFC2617.PARAM_SEPARATOR).append(" ");
        appendParameter(buf, RFC2617.PARAM_REALM, realm);
        buf.append(RFC2617.PARAM_SEPARATOR).append(" ");
        appendParameter(buf, RFC2617.PARAM_NONCE, nonce);
        buf.append(RFC2617.PARAM_SEPARATOR).append(" ");
        appendParameter(buf, RFC2617.PARAM_URI, requestUri);
        buf.append(RFC2617.PARAM_SEPARATOR).append(" ");
        appendParameter(buf, RFC2617.PARAM_URI, requestUri);
        buf.append(RFC2617.PARAM_SEPARATOR).append(" ");
        appendParameter(buf, RFC2617.PARAM_RESPONSE, digest);
        sipHeaders.add(new SipHeaderFieldName(RFC3261.HDR_AUTHORIZATION),
                new SipHeaderFieldValue(buf.toString()));
    }
    
    private void appendParameter(StringBuffer buf, String name, String value) {
        buf.append(name);
        buf.append(RFC2617.PARAM_VALUE_SEPARATOR);
        buf.append(RFC2617.PARAM_VALUE_DELIMITER);
        buf.append(value);
        buf.append(RFC2617.PARAM_VALUE_DELIMITER);
    }
}
