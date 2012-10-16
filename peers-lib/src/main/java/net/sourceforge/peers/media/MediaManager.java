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
    
    Copyright 2010, 2012 Yohann Martineau 
*/

package net.sourceforge.peers.media;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.rtp.RtpPacket;
import net.sourceforge.peers.rtp.RtpSession;
import net.sourceforge.peers.sdp.Codec;
import net.sourceforge.peers.sip.core.useragent.UserAgent;

public class MediaManager {

    public static final int DEFAULT_CLOCK = 8000; // Hz

    private UserAgent userAgent;
    private CaptureRtpSender captureRtpSender;
    private IncomingRtpReader incomingRtpReader;
    private RtpSession rtpSession;
    private DtmfFactory dtmfFactory;
    private Logger logger;
    private DatagramSocket datagramSocket;
    private FileReader fileReader;

    public MediaManager(UserAgent userAgent, Logger logger) {
        this.userAgent = userAgent;
        this.logger = logger;
        dtmfFactory = new DtmfFactory();
    }

    private void startRtpSessionOnSuccessResponse(String localAddress,
            String remoteAddress, int remotePort, Codec codec,
            SoundSource soundSource) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(localAddress);
        } catch (UnknownHostException e) {
            logger.error("unknown host: " + localAddress, e);
            return;
        }
        
        rtpSession = new RtpSession(inetAddress, datagramSocket,
                userAgent.isMediaDebug(), logger, userAgent.getPeersHome());
        
        try {
            inetAddress = InetAddress.getByName(remoteAddress);
            rtpSession.setRemoteAddress(inetAddress);
        } catch (UnknownHostException e) {
            logger.error("unknown host: " + remoteAddress, e);
        }
        rtpSession.setRemotePort(remotePort);
        
        
        try {
            captureRtpSender = new CaptureRtpSender(rtpSession,
                    soundSource, userAgent.isMediaDebug(), codec, logger,
                    userAgent.getPeersHome());
        } catch (IOException e) {
            logger.error("input/output error", e);
            return;
        }

        try {
            captureRtpSender.start();
        } catch (IOException e) {
            logger.error("input/output error", e);
        }
    }

    public void successResponseReceived(String localAddress,
            String remoteAddress, int remotePort, Codec codec) {
        switch (userAgent.getMediaMode()) {
        case captureAndPlayback:
            SoundManager soundManager = userAgent.getSoundManager();
            soundManager.openAndStartLines();
            
            startRtpSessionOnSuccessResponse(localAddress, remoteAddress,
                    remotePort, codec, soundManager);
            
            try {
                incomingRtpReader = new IncomingRtpReader(
                        captureRtpSender.getRtpSession(), soundManager, codec,
                        logger);
            } catch (IOException e) {
                logger.error("input/output error", e);
                return;
            }

            incomingRtpReader.start();
            break;

        case echo:
            Echo echo;
            try {
                echo = new Echo(datagramSocket, remoteAddress, remotePort,
                        logger);
            } catch (UnknownHostException e) {
                logger.error("unknown host amongst "
                        + localAddress + " or " + remoteAddress);
                return;
            }
            userAgent.setEcho(echo);
            Thread echoThread = new Thread(echo);
            echoThread.start();
            break;
        case file:
            String fileName = userAgent.getConfig().getMediaFile();
            fileReader = new FileReader(fileName, logger);
            startRtpSessionOnSuccessResponse(localAddress, remoteAddress,
                    remotePort, codec, fileReader);
            try {
                incomingRtpReader = new IncomingRtpReader(
                        captureRtpSender.getRtpSession(), null, codec,
                        logger);
            } catch (IOException e) {
                logger.error("input/output error", e);
                return;
            }

            incomingRtpReader.start();
            break;
        case none:
        default:
            break;
        }
    }

    private void stopPreviousRtpSession() {
      //TODO this could be optimized, create captureRtpSender at stack init
        //     and just retrieve it here
        if (rtpSession != null) {
            rtpSession.stop();
            while (!rtpSession.isSocketClosed()) {
                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    logger.debug("sleep interrupted");
                }
            }
        }
        if (captureRtpSender != null) {
            captureRtpSender.stop();
        }
    }

    private void startNewRtpSession(String destAddress, int destPort,
            Codec codec, SoundSource soundSource) {
        rtpSession = new RtpSession(
                userAgent.getConfig().getLocalInetAddress(),
                datagramSocket, userAgent.isMediaDebug(), logger,
                userAgent.getPeersHome());
        
        try {
            InetAddress inetAddress = InetAddress.getByName(destAddress);
            rtpSession.setRemoteAddress(inetAddress);
        } catch (UnknownHostException e) {
            logger.error("unknown host: " + destAddress, e);
        }
        rtpSession.setRemotePort(destPort);
        
        try {
            captureRtpSender = new CaptureRtpSender(rtpSession,
                    soundSource, userAgent.isMediaDebug(), codec, logger,
                    userAgent.getPeersHome());
        } catch (IOException e) {
            logger.error("input/output error", e);
            return;
        }
        try {
            captureRtpSender.start();
        } catch (IOException e) {
            logger.error("input/output error", e);
        }
    }

    public void handleAck(String destAddress, int destPort, Codec codec) {
        switch (userAgent.getMediaMode()) {
        case captureAndPlayback:

            stopPreviousRtpSession();

            SoundManager soundManager = userAgent.getSoundManager();
            soundManager.closeLines();
            soundManager.openAndStartLines();

            startNewRtpSession(destAddress, destPort, codec, soundManager);

            try {
                //TODO retrieve port from SDP offer
//                        incomingRtpReader = new IncomingRtpReader(localAddress,
//                                Utils.getInstance().getRtpPort(),
//                                remoteAddress, remotePort);
                //FIXME RTP sessions can be different !
                incomingRtpReader = new IncomingRtpReader(rtpSession,
                        soundManager, codec, logger);
            } catch (IOException e) {
                logger.error("input/output error", e);
                return;
            }

            incomingRtpReader.start();
            break;
        case echo:
            Echo echo;
            try {
                echo = new Echo(datagramSocket, destAddress, destPort, logger);
            } catch (UnknownHostException e) {
                logger.error("unknown host amongst "
                        + userAgent.getConfig().getLocalInetAddress()
                            .getHostAddress() + " or " + destAddress);
                return;
            }
            userAgent.setEcho(echo);
            Thread echoThread = new Thread(echo);
            echoThread.start();
            break;
        case file:
            stopPreviousRtpSession();
            if (fileReader != null) {
                fileReader.close();
            }
            String fileName = userAgent.getConfig().getMediaFile();
            fileReader = new FileReader(fileName, logger);
            startNewRtpSession(destAddress, destPort, codec, fileReader);
            try {
                incomingRtpReader = new IncomingRtpReader(rtpSession,
                        null, codec, logger);
            } catch (IOException e) {
                logger.error("input/output error", e);
                return;
            }
            incomingRtpReader.start();
            break;
        case none:
        default:
            break;
        }
    }

    public void sendDtmf(char digit) {
        if (captureRtpSender != null) {
            List<RtpPacket> rtpPackets = dtmfFactory.createDtmfPackets(digit);
            RtpSender rtpSender = captureRtpSender.getRtpSender();
            rtpSender.pushPackets(rtpPackets);
        }
    }

    public void stopSession() {
        if (rtpSession != null) {
            rtpSession.stop();
            while (!rtpSession.isSocketClosed()) {
                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    logger.debug("sleep interrupted");
                }
            }
            rtpSession = null;
        }
        if (incomingRtpReader != null) {
            incomingRtpReader = null;
        }
        if (captureRtpSender != null) {
            captureRtpSender.stop();
            captureRtpSender = null;
        }
        if (datagramSocket != null) {
            datagramSocket = null;
        }
    }

    public void setDatagramSocket(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    public FileReader getFileReader() {
        return fileReader;
    }

}
