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
    
    Copyright 2010-2013 Yohann Martineau 
*/

package net.sourceforge.peers.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.sourceforge.peers.Config;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.media.AbstractSoundManager;
import net.sourceforge.peers.media.MediaManager;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.SipMessage;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

public class EventManager implements SipListener, MainFrameListener,
        CallFrameListener, ActionListener {

    public static final String PEERS_URL = "http://peers.sourceforge.net/";
    public static final String PEERS_USER_MANUAL = PEERS_URL + "user_manual";

    public static final String ACTION_EXIT          = "Exit";
    public static final String ACTION_ACCOUNT       = "Account";
    public static final String ACTION_PREFERENCES   = "Preferences";
    public static final String ACTION_ABOUT         = "About";
    public static final String ACTION_DOCUMENTATION = "Documentation";

    private UserAgent userAgent;
    private MainFrame mainFrame;
    private AccountFrame accountFrame;
    private Map<String, CallFrame> callFrames;
    private boolean closed;
    private Logger logger;

    public EventManager(MainFrame mainFrame, String peersHome,
            Logger logger, AbstractSoundManager soundManager) {
        this.mainFrame = mainFrame;
        this.logger = logger;
        callFrames = Collections.synchronizedMap(
                new HashMap<String, CallFrame>());
        closed = false;
        // create sip stack
        try {
            userAgent = new UserAgent(this, peersHome, logger, soundManager);
        } catch (SocketException e) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "Peers sip port " +
                    		"unavailable, about to leave", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            });
        }
    }

    // sip events

    // never update gui from a non-swing thread, thus using
    // SwingUtilties.invokeLater for each event coming from sip stack.
    @Override
    public void registering(final SipRequest sipRequest) {
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                if (accountFrame != null) {
                    accountFrame.registering(sipRequest);
                }
                mainFrame.registering(sipRequest);
            }
        });

    }

    @Override
    public void registerFailed(final SipResponse sipResponse) {
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                //mainFrame.setLabelText("Registration failed");
                if (accountFrame != null) {
                    accountFrame.registerFailed(sipResponse);
                }
                mainFrame.registerFailed(sipResponse);
            }
        });

    }

    @Override
    public void registerSuccessful(final SipResponse sipResponse) {
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                if (closed) {
                    userAgent.close();
                    System.exit(0);
                    return;
                }
                if (accountFrame != null) {
                    accountFrame.registerSuccess(sipResponse);
                }
                mainFrame.registerSuccessful(sipResponse);
            }
        });

    }

    @Override
    public void calleePickup(final SipResponse sipResponse) {
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                CallFrame callFrame = getCallFrame(sipResponse);
                if (callFrame != null) {
                    callFrame.calleePickup();
                }
            }
        });

    }

    @Override
    public void error(final SipResponse sipResponse) {
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                CallFrame callFrame = getCallFrame(sipResponse);
                if (callFrame != null) {
                    callFrame.error(sipResponse);
                }
            }
        });

    }

    @Override
    public void incomingCall(final SipRequest sipRequest,
            SipResponse provResponse) {
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                SipHeaders sipHeaders = sipRequest.getSipHeaders();
                SipHeaderFieldName sipHeaderFieldName =
                    new SipHeaderFieldName(RFC3261.HDR_FROM);
                SipHeaderFieldValue from = sipHeaders.get(sipHeaderFieldName);
                final String fromValue = from.getValue();
                String callId = Utils.getMessageCallId(sipRequest);
                CallFrame callFrame = new CallFrame(fromValue, callId,
                        EventManager.this, logger);
                callFrames.put(callId, callFrame);
                callFrame.setSipRequest(sipRequest);
                callFrame.incomingCall();
            }
        });

    }

    @Override
    public void remoteHangup(final SipRequest sipRequest) {
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                CallFrame callFrame = getCallFrame(sipRequest);
                if (callFrame != null) {
                    callFrame.remoteHangup();
                }
            }
        });

    }

    @Override
    public void ringing(final SipResponse sipResponse) {
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                CallFrame callFrame = getCallFrame(sipResponse);
                if (callFrame != null) {
                    callFrame.ringing();
                }
            }
        });

    }

    // main frame events

    @Override
    public void register() {
        if (userAgent == null) {
            // if several peers instances are launched concurrently,
            // display error message and exit
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                Config config = userAgent.getConfig();
                if (config.getPassword() != null) {
                    try {
                        userAgent.register();
                    } catch (SipUriSyntaxException e) {
                        mainFrame.setLabelText(e.getMessage());
                    }
                }
            }
        });

    }

    @Override
    public void callClicked(final String uri) {
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                String callId = Utils.generateCallID(
                        userAgent.getConfig().getLocalInetAddress());
                CallFrame callFrame = new CallFrame(uri, callId,
                        EventManager.this, logger);
                callFrames.put(callId, callFrame);
                SipRequest sipRequest;
                try {
                    sipRequest = userAgent.invite(uri, callId);
                } catch (SipUriSyntaxException e) {
                    logger.error(e.getMessage(), e);
                    mainFrame.setLabelText(e.getMessage());
                    return;
                }
                callFrame.setSipRequest(sipRequest);
                callFrame.callClicked();
            }
        });

    }

    @Override
    public void windowClosed() {
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                try {
                    userAgent.unregister();
                } catch (Exception e) {
                    logger.error("error while unregistering", e);
                }
                closed = true;
                try {
                    Thread.sleep(3 * RFC3261.TIMER_T1);
                } catch (InterruptedException e) {
                }
                System.exit(0);
            }
        });
    }

    // call frame events
    
    @Override
    public void hangupClicked(final SipRequest sipRequest) {
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                userAgent.terminate(sipRequest);
            }
        });
    }

    @Override
    public void pickupClicked(final SipRequest sipRequest) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String callId = Utils.getMessageCallId(sipRequest);
                DialogManager dialogManager = userAgent.getDialogManager();
                Dialog dialog = dialogManager.getDialog(callId);
                userAgent.acceptCall(sipRequest, dialog);
            }
        });
    }
    
    @Override
    public void busyHereClicked(final SipRequest sipRequest) {
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                userAgent.rejectCall(sipRequest);
            }
        });

    }
    
    @Override
    public void dtmf(final char digit) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MediaManager mediaManager = userAgent.getMediaManager();
                mediaManager.sendDtmf(digit);
            }
        });
    }

    private CallFrame getCallFrame(SipMessage sipMessage) {
        String callId = Utils.getMessageCallId(sipMessage);
        return callFrames.get(callId);
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        logger.debug("gui actionPerformed() " + action);
        Runnable runnable = null;
        if (ACTION_EXIT.equals(action)) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    windowClosed();
                }
            };
        } else if (ACTION_ACCOUNT.equals(action)) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    if (accountFrame == null ||
                            !accountFrame.isDisplayable()) {
                        accountFrame = new AccountFrame(userAgent, logger);
                        accountFrame.setVisible(true);
                    } else {
                        accountFrame.requestFocus();
                    }
                }
            };
        } else if (ACTION_PREFERENCES.equals(action)) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "Not implemented yet");
                }
            };
        } else if (ACTION_ABOUT.equals(action)) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    AboutFrame aboutFrame = new AboutFrame(
                            userAgent.getPeersHome(), logger);
                    aboutFrame.setVisible(true);
                }
            };
        } else if (ACTION_DOCUMENTATION.equals(action)) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        URI uri = new URI(PEERS_USER_MANUAL);
                        java.awt.Desktop.getDesktop().browse(uri);
                    } catch (URISyntaxException e) {
                        logger.error(e.getMessage(), e);
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            };
        }
        if (runnable != null) {
            SwingUtilities.invokeLater(runnable);
        }
    }

}
