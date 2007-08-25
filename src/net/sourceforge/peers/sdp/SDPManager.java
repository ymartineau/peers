/*
    This file is part of Peers.

    Peers is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Peers is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    
    Copyright 2007 Yohann Martineau 
*/

package net.sourceforge.peers.sdp;

import java.io.IOException;

import net.sourceforge.peers.media.CaptureRtpSender;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.UserAgent;

//TODO ekiga -d 4 > ekiga-debug.txt 2>&1
public class SDPManager {

    public static String SUCCESS_MODEL =
        "v=0\r\n" +
        "o=user1 53655765 2353687637 IN IP4 192.168.2.2\r\n" +
        "s=-\r\n" +
        "c=IN IP4 192.168.2.2\r\n" +
        "t=0 0\r\n" +
        "m=audio 6000 RTP/AVP 0\r\n" +
        "a=rtpmap:0 PCMU/8000\r\n";

    public static String FAILURE_MODEL =
        "v=0\r\n" +
        "o=user1 53655765 2353687637 IN IP4 192.168.2.2\r\n" +
        "s=-\r\n" +
        "c=IN IP4 192.168.2.2\r\n" +
        "t=0 0\r\n" +
        "a=inactive\r\n" +
        "m=audio 6000 RTP/AVP 0\r\n" +
        "a=rtpmap:0 PCMU/8000\r\n";
    
    private SdpParser sdpParser;
    
    public SDPManager() {
        sdpParser = new SdpParser();
    }
    
    public SessionDescription handleAnswer(byte[] answer) {
        try {
            return sdpParser.parse(answer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public String handleOffer(byte[] offer) throws NoCodecException {
        // TODO generate dynamic content
        SessionDescription sessionDescription;
        try {
            sessionDescription = sdpParser.parse(offer);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        String destAddress = sessionDescription.getIpAddress().getHostAddress();
        int destPort = sessionDescription.getMedias().get(0).getPort();
        
        CaptureRtpSender sender;
        try {
            sender = new CaptureRtpSender(
                    Utils.getInstance().getMyAddress().getHostAddress(),
                    6000, // TODO make it configurable
                    destAddress, destPort);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        UserAgent.getInstance().setCaptureRtpSender(sender);
        try {
            sender.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//        }
//        sender.stop();
        return SUCCESS_MODEL;
    }
    
    public String generateErrorResponse() {
        // TODO generate dynamic content
        return FAILURE_MODEL;
    }
    
    public String generateOffer() {
        return SUCCESS_MODEL;
    }
}
