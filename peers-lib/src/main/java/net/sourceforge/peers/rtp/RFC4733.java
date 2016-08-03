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

public class RFC4733 {

    // payload types

    public static final int PAYLOAD_TYPE_TELEPHONE_EVENT = 101;

    // encoding names

    public static final String TELEPHONE_EVENT = "telephone-event";


    // DTMF values

    public static enum DTMFEvent {
        DTMF_DIGIT_0     (0),
        DTMF_DIGIT_1     (1),
        DTMF_DIGIT_2     (2),
        DTMF_DIGIT_3     (3),
        DTMF_DIGIT_4     (4),
        DTMF_DIGIT_5     (5),
        DTMF_DIGIT_6     (6),
        DTMF_DIGIT_7     (7),
        DTMF_DIGIT_8     (8),
        DTMF_DIGIT_9     (9),
        DTMF_DIGIT_STAR  (10),
        DTMF_DIGIT_HASH  (11),
        DTMF_DIGIT_A     (12),
        DTMF_DIGIT_B     (13),
        DTMF_DIGIT_C     (14),
        DTMF_DIGIT_D     (15),
        DTMF_DIGIT_FLASH (16);

        private int value;

        DTMFEvent(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static DTMFEvent fromValue(int value) {
            for (DTMFEvent type : DTMFEvent.values()) {
                if (type.getValue() == value) {
                    return type;
                }
            }
            return null;
        }
    }

}
