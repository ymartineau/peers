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
    
    Copyright 2010 Yohann Martineau 
*/

package net.sourceforge.peers.media;

import java.io.IOException;
import java.net.UnknownHostException;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.sdp.Codec;
import net.sourceforge.peers.sip.core.useragent.UserAgent;

public class MediaManager {

    public static final int DEFAULT_CLOCK = 8000; // Hz

    private UserAgent userAgent;

    public MediaManager(UserAgent userAgent) {
        super();
        this.userAgent = userAgent;
    }

    public void successResponseReceived(String localAddress,
            String remoteAddress, int remotePort, Codec codec) {
        switch (userAgent.getMediaMode()) {
        case captureAndPlayback:
            CaptureRtpSender captureRtpSender;
            //TODO this could be optimized, create captureRtpSender at stack init
            //     and just retrieve it here
            SoundManager soundManager = userAgent.getSoundManager();
            soundManager.openAndStartLines();
            try {
                captureRtpSender = new CaptureRtpSender(localAddress,
                        userAgent.getRtpPort(),
                        remoteAddress, remotePort, soundManager,
                        userAgent.isMediaDebug(), codec);
            } catch (IOException e) {
                Logger.error("input/output error", e);
                return;
            }
            userAgent.setCaptureRtpSender(captureRtpSender);

            try {
                captureRtpSender.start();
            } catch (IOException e) {
                Logger.error("input/output error", e);
            }
            
            IncomingRtpReader incomingRtpReader;
            try {
                //TODO retrieve port from SDP offer
//                    incomingRtpReader = new IncomingRtpReader(localAddress,
//                            Utils.getInstance().getRtpPort(),
//                            remoteAddress, remotePort);
                incomingRtpReader = new IncomingRtpReader(
                        captureRtpSender.getRtpSession(), soundManager, codec);
            } catch (IOException e) {
                Logger.error("input/output error", e);
                return;
            }
            userAgent.setIncomingRtpReader(incomingRtpReader);

            incomingRtpReader.start();
            break;

        case echo:
            Echo echo;
            try {
                echo = new Echo(localAddress, userAgent.getRtpPort(),
                            remoteAddress, remotePort);
            } catch (UnknownHostException e) {
                Logger.error("unknown host amongst "
                        + localAddress + " or " + remoteAddress);
                return;
            }
            userAgent.setEcho(echo);
            Thread echoThread = new Thread(echo);
            echoThread.start();
            break;
        case none:
        default:
            break;
        }
    }

    public void handleAck(String destAddress, int destPort, Codec codec) {
        switch (userAgent.getMediaMode()) {
        case captureAndPlayback:
            //TODO this could be optimized, create captureRtpSender at stack init
            //     and just retrieve it here
            CaptureRtpSender captureRtpSender;
            captureRtpSender = userAgent.getCaptureRtpSender();
            IncomingRtpReader incomingRtpReader =
                userAgent.getIncomingRtpReader();
            if (incomingRtpReader != null) {
                incomingRtpReader.stop();
            }
            if (captureRtpSender != null) {
                captureRtpSender.stop();
                while (!captureRtpSender.isTerminated()) {
                    try {
                        Thread.sleep(15);
                    } catch (InterruptedException e) {
                        Logger.debug("sleep interrupted");
                    }
                }
            }
            SoundManager soundManager = userAgent.getSoundManager();
            soundManager.closeLines();
            soundManager.openAndStartLines();
            try {
                captureRtpSender = new CaptureRtpSender(userAgent.getConfig()
                            .getLocalInetAddress().getHostAddress(),
                        userAgent.getRtpPort(), destAddress, destPort,
                        soundManager, userAgent.isMediaDebug(), codec);
            } catch (IOException e) {
                Logger.error("input/output error", e);
                return;
            }
            userAgent.setCaptureRtpSender(captureRtpSender);
            try {
                captureRtpSender.start();
            } catch (IOException e) {
                Logger.error("input/output error", e);
            }
            try {
                //TODO retrieve port from SDP offer
//                        incomingRtpReader = new IncomingRtpReader(localAddress,
//                                Utils.getInstance().getRtpPort(),
//                                remoteAddress, remotePort);
                //FIXME RTP sessions can be different !
                incomingRtpReader = new IncomingRtpReader(
                        captureRtpSender.getRtpSession(), soundManager, codec);
            } catch (IOException e) {
                Logger.error("input/output error", e);
                return;
            }
            userAgent.setIncomingRtpReader(incomingRtpReader);

            incomingRtpReader.start();
            break;
        case echo:
            Echo echo;
            try {
                echo = new Echo(userAgent.getConfig().getLocalInetAddress()
                            .getHostAddress(),
                        userAgent.getRtpPort(), destAddress, destPort);
            } catch (UnknownHostException e) {
                Logger.error("unknown host amongst "
                        + userAgent.getConfig().getLocalInetAddress()
                            .getHostAddress() + " or " + destAddress);
                return;
            }
            userAgent.setEcho(echo);
            Thread echoThread = new Thread(echo);
            echoThread.start();
            break;
        case none:
        default:
            break;
        }
    }
}
