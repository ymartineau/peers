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

package net.sourceforge.peers.rtp;

import net.sourceforge.peers.FileLogger;

import org.testng.annotations.Test;

public class RtpParserTestNG {

    @Test
    public void testDecode() {
        byte[] packet = new byte[172];
        int pos = 0;
        packet[pos++] = new Integer(0x80).byteValue();
        packet[pos++] = new Integer(0x00).byteValue();
        packet[pos++] = new Integer(0x02).byteValue();
        packet[pos++] = new Integer(0xaf).byteValue();
        packet[pos++] = new Integer(0x00).byteValue();
        packet[pos++] = new Integer(0x00).byteValue();
        packet[pos++] = new Integer(0x01).byteValue();
        packet[pos++] = new Integer(0x8d).byteValue();
        packet[pos++] = new Integer(0x00).byteValue();
        packet[pos++] = new Integer(0x00).byteValue();
        packet[pos++] = new Integer(0x00).byteValue();
        packet[pos++] = new Integer(0x01).byteValue();
        for (int i = 0; i < 160; ++i) {
            packet[pos++] = new Integer(i).byteValue();
        }
        RtpParser rtpParser = new RtpParser(new FileLogger(null));
        RtpPacket rtpPacket = rtpParser.decode(packet);
        assert rtpPacket.getVersion() == 2;
        assert !rtpPacket.isPadding();
        assert !rtpPacket.isExtension();
        assert rtpPacket.getCsrcCount() == 0;
        assert !rtpPacket.isMarker();
        assert rtpPacket.getPayloadType() == 0;
        assert rtpPacket.getSequenceNumber() == 687;
        assert rtpPacket.getTimestamp() == 397;
        assert rtpPacket.getSsrc() == 1;
        assert rtpPacket.getData().length == 160;
        for (int i = 0; i < 160; ++i)
            assert (int)(rtpPacket.getData()[i] & 0xff) == i;
    }

    @Test
    public void testEncode() {
        RtpPacket rtpPacket = new RtpPacket();
        rtpPacket.setVersion(2);
        rtpPacket.setPadding(false);
        rtpPacket.setExtension(false);
        rtpPacket.setCsrcCount(0);
        rtpPacket.setMarker(false);
        rtpPacket.setPayloadType(0);
        rtpPacket.setSequenceNumber(687);
        rtpPacket.setTimestamp(397);
        rtpPacket.setSsrc(1);
        byte[] data = new byte[160];
        for (int i = 0; i < data.length; ++i) {
            data[i] = new Integer(i).byteValue();
        }
        rtpPacket.setData(data);
        RtpParser rtpParser = new RtpParser(new FileLogger(null));
        byte[] packet = rtpParser.encode(rtpPacket);
        int pos = 0;
        assert packet[pos++] == new Integer(0x80).byteValue();
        assert packet[pos++] == new Integer(0x00).byteValue();
        assert packet[pos++] == new Integer(0x02).byteValue();
        assert packet[pos++] == new Integer(0xaf).byteValue();
        assert packet[pos++] == new Integer(0x00).byteValue();
        assert packet[pos++] == new Integer(0x00).byteValue();
        assert packet[pos++] == new Integer(0x01).byteValue();
        assert packet[pos++] == new Integer(0x8d).byteValue();
        assert packet[pos++] == new Integer(0x00).byteValue();
        assert packet[pos++] == new Integer(0x00).byteValue();
        assert packet[pos++] == new Integer(0x00).byteValue();
        assert packet[pos++] == new Integer(0x01).byteValue();
        for (int i = 0; i < rtpPacket.getData().length; ++i)
            assert packet[pos++] == data[i];
    }

}
