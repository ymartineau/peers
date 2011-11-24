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
    
    Copyright 2007, 2008, 2009, 2010 Yohann Martineau 
*/

package net.sourceforge.peers.sdp;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.List;

import org.testng.annotations.Test;

public class SdpParserTestNG {

    @Test
    public void testParse() throws IOException {
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
        sessionDescription = sdpParser.parse(body.getBytes());
        assert sessionDescription != null;
        assert "jdoe".equals(sessionDescription.getUsername());
        assert 2890844526L == sessionDescription.getId();
        assert 2890842807L == sessionDescription.getVersion();
        assert InetAddress.getByName("224.2.17.12").equals(
                    sessionDescription.getIpAddress());
        assert "SDP Seminar".equals(sessionDescription.getName());
        
        Hashtable<String, String> sessionAttributes =
            sessionDescription.getAttributes();
        assert sessionAttributes != null;
        assert 1 == sessionAttributes.size();
        assert "".equals(sessionAttributes.get("recvonly"));
        
        List<MediaDescription> mediaDescriptions =
            sessionDescription.getMediaDescriptions();
        assert mediaDescriptions != null;
        assert mediaDescriptions.size() == 2;
        
        MediaDescription audioMedia = mediaDescriptions.get(0);
        assert InetAddress.getByName("224.2.17.12").equals(
                    audioMedia.getIpAddress());
        assert 49170 == audioMedia.getPort();
        
        MediaDescription videoMedia = mediaDescriptions.get(1);
        assert InetAddress.getByName("224.2.17.12").equals(
                    videoMedia.getIpAddress());
        assert 51372 == videoMedia.getPort();
        Hashtable<String, String> videoAttributes = videoMedia.getAttributes();
        assert videoAttributes != null;
        assert 0 == videoAttributes.size();
        List<Codec> codecs = videoMedia.getCodecs();
        assert codecs != null;
        assert codecs.size() == 1;
        Codec codec = codecs.get(0);
        assert codec.getPayloadType() == 99;
        assert "h263-1998".equals(codec.getName());
    }

}
