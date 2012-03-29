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
    
    Copyright 2011 Yohann Martineau 
*/

package net.sourceforge.peers.javascript;

import java.applet.Applet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.media.MediaManager;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

public class JsUserAgent extends Applet implements SipListener {
    
    private static final long serialVersionUID = 1L;

    private UserAgent userAgent;
    private Logger logger;

    @Override
    public void init() {
        System.out.println("init");
        String peersDir = ".peers";
        String home = System.getProperty("user.home");
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String peersHome = home + File.separator + peersDir + File.separator
            + format.format(new Date());
        createDirectory(peersHome + File.separator + "conf");
        createDirectory(peersHome + File.separator + "logs");
        createDirectory(peersHome + File.separator + "media");
        copyFile("conf/peers.xml", peersHome + File.separator + "conf"
                + File.separator + "peers.xml");
        copyFile("conf/peers.xsd", peersHome + File.separator + "conf"
                + File.separator + "peers.xsd");
        final String peersPath = new File(peersHome).getAbsolutePath();
        logger = new Logger(peersPath);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                
                try {
                    userAgent = new UserAgent(JsUserAgent.this, peersPath,
                            logger);
                    userAgent.getUac().register();
                } catch (SocketException e) {
                    logger.error(e.getMessage());
                } catch (SipUriSyntaxException e) {
                    logger.error(e.getMessage());
                }
            }
        });

        thread.start();
    }

    private static void createDirectory(String dir) {
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
    }
    
    private static void copyFile(String source, String dest) {
        ClassLoader classLoader = JsUserAgent.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(source);
        try {
            FileOutputStream out = new FileOutputStream(dest);
            byte[] buf = new byte[256];
            int readBytes;
            while ((readBytes = inputStream.read(buf)) != -1) {
                out.write(buf, 0, readBytes);
            }
            out.close();
        } catch (IOException e) {
            System.err.println(e);
        }


    }

    // client methods

    public void close() {
        userAgent.close();
    }

    public Config getConfig() {
        return userAgent.getConfig();
    }

    public void register() {
        try {
            userAgent.getUac().register();
        } catch (SipUriSyntaxException e) {
            logger.error(e.getMessage());
        }
    }

    public void invite(final String uri) {
        System.out.println("invite " + uri);

        Thread thread = new Thread(new Runnable() {
            public void run() {
                String callId = Utils.generateCallID(
                        userAgent.getConfig().getLocalInetAddress());
                try {
                    userAgent.getUac().invite(uri, callId);
                } catch (SipUriSyntaxException e) {
                    logger.error(e.getMessage());
                }
            }
        }, "invite-thread");
        thread.start();

    }

    public void unregister() {
        try {
            userAgent.getUac().unregister();
        } catch (SipUriSyntaxException e) {
            logger.error(e.getMessage());
        }
    }

    public void terminate(SipRequest sipRequest) {
        userAgent.getUac().terminate(sipRequest);
    }

    public void pickupClicked(SipRequest sipRequest) {
        String callId = Utils.getMessageCallId(sipRequest);
        DialogManager dialogManager = userAgent.getDialogManager();
        Dialog dialog = dialogManager.getDialog(callId);
        userAgent.getUas().acceptCall(sipRequest, dialog);
    }
    
    public void busyHereClicked(SipRequest sipRequest) {
        userAgent.getUas().rejectCall(sipRequest);
    }
    
    public void dtmf(char digit) {
        MediaManager mediaManager = userAgent.getMediaManager();
        mediaManager.sendDtmf(digit);
    }

    // server methods

    @Override
    public void registering(SipRequest sipRequest) {
        try {
            JSObject window = JSObject.getWindow(this);
            window.call("registering", new Object[]{sipRequest});
        } catch (JSException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void registerSuccessful(SipResponse sipResponse) {
        try {
            JSObject window = JSObject.getWindow(this);
            window.call("registerSuccessful", new Object[]{sipResponse});
        } catch (JSException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void registerFailed(SipResponse sipResponse) {
        try {
            JSObject window = JSObject.getWindow(this);
            window.call("registerFailed", new Object[]{sipResponse});
        } catch (JSException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void incomingCall(SipRequest sipRequest,
            SipResponse provResponse) { }

    @Override
    public void remoteHangup(SipRequest sipRequest) {
        try {
            JSObject window = JSObject.getWindow(this);
            window.call("remoteHangup", new Object[]{sipRequest});
        } catch (JSException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void ringing(SipResponse sipResponse) {
        try {
            JSObject window = JSObject.getWindow(this);
            window.call("ringing", new Object[]{sipResponse});
        } catch (JSException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void calleePickup(SipResponse sipResponse) {
        try {
            JSObject window = JSObject.getWindow(this);
            window.call("calleePickup", new Object[]{sipResponse});
        } catch (JSException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void error(SipResponse sipResponse) {
        try {
            JSObject window = JSObject.getWindow(this);
            window.call("error", new Object[]{sipResponse});
        } catch (JSException e) {
            logger.error(e.getMessage());
        }
    }

}
