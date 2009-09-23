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
    
    Copyright 2007, 2008, 2009 Yohann Martineau 
*/

package net.sourceforge.peers.sdp;

import gov.nist.jrtp.RtpException;

import java.io.IOException;

import net.sourceforge.peers.Logger;
import net.sourceforge.peers.media.CaptureRtpSender;
import net.sourceforge.peers.media.IncomingRtpReader;
import net.sourceforge.peers.sip.core.useragent.UserAgent;

//TODO ekiga -d 4 > ekiga-debug.txt 2>&1
public class SDPManager {
    
    private SdpParser sdpParser;
    private UserAgent userAgent;
    
    public SDPManager(UserAgent userAgent) {
        this.userAgent = userAgent;
        sdpParser = new SdpParser();
    }
    
    public SessionDescription handleAnswer(byte[] answer) {
        try {
            return sdpParser.parse(answer);
        } catch (IOException e) {
            Logger.error("input/output error", e);
        }
        return null;
    }
    
    public String handleOffer(byte[] offer)
            throws NoCodecException {
        // TODO generate dynamic content
        SessionDescription sessionDescription;
        try {
            sessionDescription = sdpParser.parse(offer);
        } catch (IOException e) {
            Logger.error("input/output error", e);
            return null;
        }
        String destAddress = sessionDescription.getIpAddress().getHostAddress();
        int destPort = sessionDescription.getMedias().get(0).getPort();
        
        if (userAgent.isMedia()) {
            //FIXME move this to InviteHandler
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
            try {
                captureRtpSender = new CaptureRtpSender(
                        userAgent.getMyAddress().getHostAddress(),
                        userAgent.getRtpPort(), destAddress, destPort);
            } catch (IOException e) {
                Logger.error("input/output error", e);
                return null;
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
                        captureRtpSender.getRtpSession());
            } catch (IOException e1) {
                Logger.error("input/output error", e1);
                return null;
            }
            userAgent.setIncomingRtpReader(incomingRtpReader);

            try {
                incomingRtpReader.start();
            } catch (IOException e1) {
                Logger.error("input/output error", e1);
            } catch (RtpException e1) {
                Logger.error("RTP error", e1);
            }
        }
        
        return generateOffer();
    }
    
    public String generateErrorResponse() {
        StringBuffer buf = generateSdpBegining();
        buf.append("a=inactive\r\n");
        return generateSdpEnd(buf);
    }
    
    public String generateOffer() {
        return generateSdpEnd(generateSdpBegining());
    }
    
    private StringBuffer generateSdpBegining() {
        String hostAddress = userAgent.getMyAddress().getHostAddress();
        StringBuffer buf = new StringBuffer();
        buf.append("v=0\r\n");
        buf.append("o=user1 53655765 2353687637 IN IP4 ").append(hostAddress).append("\r\n");
        buf.append("s=-\r\n");
        buf.append("c=IN IP4 ").append(hostAddress).append("\r\n");
        buf.append("t=0 0\r\n");
        return buf;
    }
    
    private String generateSdpEnd(StringBuffer buf) {
        buf.append("m=audio ").append(userAgent.getRtpPort()).append(" RTP/AVP 0\r\n");
        buf.append("a=rtpmap:0 PCMU/8000\r\n");
        return buf.toString();
    }

}
