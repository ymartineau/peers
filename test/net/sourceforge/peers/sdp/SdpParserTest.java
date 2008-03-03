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
    
    Copyright 2007, 2008 Yohann Martineau 
*/

package net.sourceforge.peers.sdp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;

import junit.framework.TestCase;

public class SdpParserTest extends TestCase {

    public void testParse() {
        SdpParser sdpParser = new SdpParser();
        String body = "v=0\n"
            + "o=jdoe 2890844526 2890842807 IN IP4 10.47.16.5\n"
            + "s=SDP Seminar\n"
            + "i=A Seminar on the session description protocol\n"
            + "u=http://www.example.com/seminars/sdp.pdf\n"
            + "e=j.doe@example.com (Jane Doe)\n"
            //+ "c=IN IP4 224.2.17.12/127\r"
            + "c=IN IP4 224.2.17.12\r"
            + "t=2873397496 2873404696\r\n"
            + "a=recvonly\n"
            + "m=audio 49170 RTP/AVP 0\n"
            + "m=video 51372 RTP/AVP 99\r\n"
            + "a=rtpmap:99 h263-1998/90000\r";
        SessionDescription sessionDescription;
        try {
            sessionDescription = sdpParser.parse(body.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
            return;
        }
        assertNotNull(sessionDescription);
        assertEquals("jdoe", sessionDescription.getUsername());
        assertEquals(2890844526L, sessionDescription.getId());
        assertEquals(2890842807L, sessionDescription.getVersion());
        try {
            assertEquals(InetAddress.getByName("224.2.17.12"),
                    sessionDescription.getIpAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        assertEquals("SDP Seminar", sessionDescription.getName());
        
        Hashtable<String, String> sessionAttributes =
            sessionDescription.getAttributes();
        assertNotNull(sessionAttributes);
        assertEquals(1, sessionAttributes.size());
        assertEquals("", sessionAttributes.get("recvonly"));
        
        ArrayList<MediaDescription> mediaDescriptions =
            sessionDescription.getMedias();
        assertNotNull(mediaDescriptions);
        assertTrue(mediaDescriptions.size() == 2);
        
        MediaDescription audioMedia = mediaDescriptions.get(0);
        try {
            assertEquals(InetAddress.getByName("224.2.17.12"),
                    audioMedia.getIpAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        assertEquals(49170, audioMedia.getPort());
        
        MediaDescription videoMedia = mediaDescriptions.get(1);
        try {
            assertEquals(InetAddress.getByName("224.2.17.12"),
                    videoMedia.getIpAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        assertEquals(51372, videoMedia.getPort());
        Hashtable<String, String> videoAttributes = videoMedia.getAttributes();
        assertNotNull(videoAttributes);
        assertEquals(1, videoAttributes.size());
        assertEquals("99 h263-1998/90000", videoAttributes.get("rtpmap"));
    }

}
