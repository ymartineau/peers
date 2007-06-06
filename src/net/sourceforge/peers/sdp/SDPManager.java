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

public class SDPManager {

    public static String SUCCESS_MODEL =
        "v=0\n" +
        "o=user1 53655765 2353687637 IN IP4 192.168.2.2\n" +
        "s=-\n" +
        "c=IN IP4 192.168.2.2\n" +
        "t=0 0\n" +
        "m=audio 6000 RTP/AVP 0\n" +
        "a=rtpmap:0 PCMU/8000\n";

    public static String FAILURE_MODEL =
        "v=0\n" +
        "o=user1 53655765 2353687637 IN IP4 192.168.2.2\n" +
        "s=-\n" +
        "c=IN IP4 192.168.2.2\n" +
        "t=0 0\n" +
        "a=inactive\n" +
        "m=audio 6000 RTP/AVP 0\n" +
        "a=rtpmap:0 PCMU/8000\n";
        
    public String handleOffer(String offer) throws NoCodecException {
        // TODO generate dynamic content
        return SUCCESS_MODEL;
    }
    
    public String generateErrorResponse() {
        // TODO generate dynamic content
        return FAILURE_MODEL;
    }
    
    public String generateOffer() {
        return null;
    }
}
