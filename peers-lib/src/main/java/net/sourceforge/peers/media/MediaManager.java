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

package net.sourceforge.peers.media;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.rtp.RtpPacket;
import net.sourceforge.peers.rtp.RtpSession;
import net.sourceforge.peers.sdp.Codec;
import net.sourceforge.peers.sip.core.useragent.UserAgent;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class MediaManager {

    public static final int DEFAULT_CLOCK = 8000; // Hz

    private UserAgent userAgent;
    private Object connectedSync = new Object();
    private CaptureRtpSender captureRtpSender;
    private IncomingRtpReader incomingRtpReader;
    private RtpSession rtpSession;
    private DtmfFactory dtmfFactory;
    private Logger logger;
    private DatagramSocket datagramSocket;

    private AbstractSoundManager soundManager;
    private DtmfEventHandler dtmfEventHandler;

    public MediaManager(UserAgent userAgent, DtmfEventHandler dtmfEventHandler,  Logger logger) {
        this.userAgent = userAgent;
        this.dtmfEventHandler = dtmfEventHandler;
        this.logger = logger;
        dtmfFactory = new DtmfFactory();
    }

    private void setCaptureRtpSender(CaptureRtpSender captureRtpSender) {
        this.captureRtpSender = captureRtpSender;
        synchronized (connectedSync) {
            connectedSync.notifyAll();
        }
    }

    public CaptureRtpSender getCaptureRtpSender() {
        return captureRtpSender;
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
            setCaptureRtpSender(new CaptureRtpSender(rtpSession,
                    soundSource, userAgent.isMediaDebug(), codec, logger,
                    userAgent.getPeersHome()));
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
        case file:
            if (soundManager != null) {
                soundManager.close();
            }
            soundManager = userAgent.getAbstractSoundManagerFactory().getSoundManager();
            soundManager.init();
            startRtpSessionOnSuccessResponse(localAddress, remoteAddress,
                    remotePort, codec, soundManager);
            
            try {
                incomingRtpReader = new IncomingRtpReader(
                        captureRtpSender.getRtpSession(), soundManager, codec, dtmfEventHandler,
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
            Thread echoThread = new Thread(echo, Echo.class.getSimpleName());
            echoThread.start();
            break;
        case none:
        default:
            break;
        }
    }

    private void startRtpSession(String destAddress, int destPort,
        Codec codec, SoundSource soundSource) {
        rtpSession = new RtpSession(userAgent.getConfig()
                .getLocalInetAddress(), datagramSocket,
                userAgent.isMediaDebug(), logger, userAgent.getPeersHome());

        try {
            InetAddress inetAddress = InetAddress.getByName(destAddress);
            rtpSession.setRemoteAddress(inetAddress);
        } catch (UnknownHostException e) {
            logger.error("unknown host: " + destAddress, e);
        }
        rtpSession.setRemotePort(destPort);
        
        try {
            setCaptureRtpSender(new CaptureRtpSender(rtpSession,
                    soundSource, userAgent.isMediaDebug(), codec, logger,
                    userAgent.getPeersHome()));
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
        case file:
            if (soundManager != null) {
                soundManager.close();
            }
            soundManager = userAgent.getAbstractSoundManagerFactory().getSoundManager();
            soundManager.init();

            startRtpSession(destAddress, destPort, codec, soundManager);

            try {
                //FIXME RTP sessions can be different !
                incomingRtpReader = new IncomingRtpReader(rtpSession,
                        soundManager, codec, dtmfEventHandler, logger);
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
            Thread echoThread = new Thread(echo, Echo.class.getSimpleName());
            echoThread.start();
            break;
        case none:
        default:
            break;
        }
    }

    public void updateRemote(String destAddress, int destPort, Codec codec) {
        switch (userAgent.getMediaMode()) {
        case captureAndPlayback:
        case file:
            try {
                InetAddress inetAddress = InetAddress.getByName(destAddress);
                rtpSession.setRemoteAddress(inetAddress);
            } catch (UnknownHostException e) {
                logger.error("unknown host: " + destAddress, e);
            }
            rtpSession.setRemotePort(destPort);
            break;
        case echo:
            //TODO update echo socket
            break;
        default:
            break;
        }

    }
    
    public void sendDtmf(char digit) {
        if (connected()) {
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
            setCaptureRtpSender(null);
        }
        if (datagramSocket != null) {
            datagramSocket = null;
        }

        switch (userAgent.getMediaMode()) {
        case captureAndPlayback:
        case file:
            if (soundManager != null) {
                soundManager.close();
            }
            break;
        case echo:
            Echo echo = userAgent.getEcho();
            if (echo != null) {
                echo.stop();
                userAgent.setEcho(null);
            }
            break;
        default:
            break;
        }
    }

    public void setDatagramSocket(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    public DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

    public SoundSource getSoundSource() {
        switch (userAgent.getMediaMode()) {
            case captureAndPlayback:
            case file:
                return soundManager;
            case echo:
            default:
                return null;
        }
    }

    public boolean connected() {
        return captureRtpSender != null;
    }

    public void waitConnected() throws InterruptedException {
        if (!connected()) {
            synchronized (connectedSync) {
                while (!connected()) {
                    connectedSync.wait();
                }
            }
        }
        return;

    }

    public void waitFinishedSending() throws IOException, InterruptedException {
        if (connected()) {
            captureRtpSender.getRtpSender().waitEmpty();
        }
    }

}
