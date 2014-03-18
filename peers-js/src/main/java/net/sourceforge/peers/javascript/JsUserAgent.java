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
    
    Copyright 2011-2013 Yohann Martineau 
*/

package net.sourceforge.peers.javascript;

import java.applet.Applet;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.JavaConfig;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.javaxsound.JavaxSoundManager;
import net.sourceforge.peers.media.AbstractSoundManager;
import net.sourceforge.peers.media.MediaManager;
import net.sourceforge.peers.media.MediaMode;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.syntaxencoding.SipURI;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

public class JsUserAgent extends Applet implements SipListener, WebLoggerOutput {
    
    private static final long serialVersionUID = 1L;

    private UserAgent userAgent;
    private Config config;
    private Logger logger;
    private ExecutorService executorService;

    public void instantiatePeers(String ipAddress) {
        try {
            System.out.println("instantiatePeers");
            executorService = Executors.newCachedThreadPool();
            logger = new WebLogger(this);
            config = new JavaConfig();
            String peersHome = Utils.DEFAULT_PEERS_HOME;
            final AbstractSoundManager soundManager = new JavaxSoundManager(
                    false, //TODO config.isMediaDebug(),
                    logger, peersHome);
            InetAddress inetAddress;
            try {
                inetAddress = InetAddress.getByName(ipAddress);
            } catch (UnknownHostException e1) {
                System.out.println(e1 + " " + e1.getMessage());
                logger.error("Unknown ipAddress " + ipAddress, e1);
                return;
            }
            config.setLocalInetAddress(inetAddress);
            config.setMediaMode(MediaMode.captureAndPlayback);

            executorService.submit(new Runnable() {
                @Override
                public void run() {

                    try {
                        userAgent = new UserAgent(JsUserAgent.this, config,
                                logger, soundManager);
                        System.out.println("useragent created");
                    } catch (SocketException e) {
                        logger.error(e.getMessage());
                        System.out.println(e);
                    }
                }
            });
            System.out.println("task scheduled");
        } catch (SecurityException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    // client methods

    public void close() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                userAgent.close();
            }
        });
    }

    public Config getConfig() {
        return userAgent.getConfig();
    }

    public void register(final String userPart, final String password,
            final String domain, final String outboundProxy) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                config.setUserPart(userPart);
                config.setPassword(password);
                config.setDomain(domain);
                if (outboundProxy != null && !"".equals(outboundProxy.trim())) {
                    SipURI outboundProxyUri = null;
                    try {
                        outboundProxyUri = new SipURI(outboundProxy);
                    } catch (SipUriSyntaxException e) {
                        logger.error(e.getMessage());
                    }
                    config.setOutboundProxy(outboundProxyUri);
                }

                try {
                    userAgent.register();
                } catch (SipUriSyntaxException e) {
                    logger.error(e.getMessage());
                }
            }
        });
    }

    public void invite(final String uri) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                String callId = Utils.generateCallID(userAgent.getConfig()
                        .getLocalInetAddress());
                try {
                    SipRequest sipRequest = userAgent.invite(uri, callId);
                    setInviteSipRequest(sipRequest);
                } catch (SipUriSyntaxException e) {
                    logger.error(e.getMessage());
                }
            }
        });

    }

    public void unregister() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    userAgent.unregister();
                } catch (SipUriSyntaxException e) {
                    logger.error(e.getMessage());
                }
            }
        });

    }

    public void terminate(final SipRequest sipRequest) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                userAgent.terminate(sipRequest);
            }
        });
    }

    public void pickupClicked(final SipRequest sipRequest) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                String callId = Utils.getMessageCallId(sipRequest);
                DialogManager dialogManager = userAgent.getDialogManager();
                Dialog dialog = dialogManager.getDialog(callId);
                userAgent.acceptCall(sipRequest, dialog);
            }
        });
    }
    
    public void busyHereClicked(final SipRequest sipRequest) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                userAgent.rejectCall(sipRequest);

            }
        });
    }
    
    public void dtmf(final char digit) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                MediaManager mediaManager = userAgent.getMediaManager();
                mediaManager.sendDtmf(digit);
            }
        });
    }

    // server methods

    @Override
    public void registering(final SipRequest sipRequest) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    JSObject window = JSObject.getWindow(JsUserAgent.this);
                    window.call("registering", new Object[]{sipRequest});
                } catch (JSException e) {
                    logger.error(e.getMessage());
                }
            }
        });

    }

    @Override
    public void registerSuccessful(final SipResponse sipResponse) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    JSObject window = JSObject.getWindow(JsUserAgent.this);
                    window.call("registerSuccessful", new Object[]{sipResponse});
                } catch (JSException e) {
                    logger.error(e.getMessage());
                }
            }
        });

    }

    @Override
    public void registerFailed(final SipResponse sipResponse) {
        executorService.submit(new Runnable() {
            
            @Override
            public void run() {
                try {
                    JSObject window = JSObject.getWindow(JsUserAgent.this);
                    window.call("registerFailed", new Object[]{sipResponse});
                } catch (JSException e) {
                    logger.error(e.getMessage());
                }
            }
        });

    }

    @Override
    public void incomingCall(final SipRequest sipRequest,
            final SipResponse provResponse) {
        executorService.submit(new Runnable() {
            
            @Override
            public void run() {
                try {
                    JSObject window = JSObject.getWindow(JsUserAgent.this);
                    window.call("incomingCall",
                            new Object[]{sipRequest, provResponse});
                } catch (JSException e) {
                    logger.error(e.getMessage());
                }
            }
        });
    }

    @Override
    public void remoteHangup(final SipRequest sipRequest) {
        executorService.submit(new Runnable() {
            
            @Override
            public void run() {
                try {
                    JSObject window = JSObject.getWindow(JsUserAgent.this);
                    window.call("remoteHangup", new Object[]{sipRequest});
                } catch (JSException e) {
                    logger.error(e.getMessage());
                }
            }
        });

    }

    @Override
    public void ringing(final SipResponse sipResponse) {
        executorService.submit(new Runnable() {
            
            @Override
            public void run() {
                try {
                    JSObject window = JSObject.getWindow(JsUserAgent.this);
                    window.call("ringing", new Object[]{sipResponse});
                } catch (JSException e) {
                    logger.error(e.getMessage());
                }
            }
        });

    }

    @Override
    public void calleePickup(final SipResponse sipResponse) {
        executorService.submit(new Runnable() {
            
            @Override
            public void run() {
                try {
                    JSObject window = JSObject.getWindow(JsUserAgent.this);
                    window.call("calleePickup", new Object[]{sipResponse});
                } catch (JSException e) {
                    logger.error(e.getMessage());
                }
            }
        });

    }

    @Override
    public void error(final SipResponse sipResponse) {
        executorService.submit(new Runnable() {
            
            @Override
            public void run() {
                try {
                    JSObject window = JSObject.getWindow(JsUserAgent.this);
                    window.call("error", new Object[]{sipResponse});
                } catch (JSException e) {
                    logger.error(e.getMessage());
                }
            }
        });

    }

    // WebLoggerOutput methods
    
    @Override
    public void javaLog(final String message) {
        executorService.submit(new Runnable() {
            
            @Override
            public void run() {
                try {
                    JSObject window = JSObject.getWindow(JsUserAgent.this);
                    window.call("javaLog", new Object[]{message});
                } catch (JSException e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void javaNetworkLog(final String message) {
        executorService.submit(new Runnable() {
            
            @Override
            public void run() {
                try {
                    JSObject window = JSObject.getWindow(JsUserAgent.this);
                    window.call("javaNetworkLog", new Object[]{message});
                } catch (JSException e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

    }

    // utility

    public void setInviteSipRequest(final SipRequest sipRequest) {
        executorService.submit(new Runnable() {
            
            @Override
            public void run() {
                try {
                    JSObject window = JSObject.getWindow(JsUserAgent.this);
                    window.call("setInviteSipRequest", new Object[]{sipRequest});
                } catch (JSException e) {
                    logger.error(e.getMessage());
                }
            }
        });

    }

    public String getFrom(SipRequest sipRequest) {
        SipHeaders sipHeaders = sipRequest.getSipHeaders();
        SipHeaderFieldName sipHeaderFieldName =
            new SipHeaderFieldName(RFC3261.HDR_FROM);
        SipHeaderFieldValue from = sipHeaders.get(sipHeaderFieldName);
        return from.getValue();
    }
}
