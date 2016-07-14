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
    
    Copyright 2012 Yohann Martineau
*/

package net.sourceforge.peers.media;

public interface SoundSource {

    enum DataFormat {
        LINEAR_PCM_8KHZ_16BITS_SIGNED_MONO_LITTLE_ENDIAN("pcm_8khz_16_bits_mono", "Linear PCM, 8kHz, 16-bites signed, mono-channel, little endian"),
        ALAW_8KHZ_MONO_LITTLE_ENDIAN("a_law", "A-law, 8kHz, mono-channel, little endian");

        private String shortAlias;
        private String description;

        public static DataFormat DEFAULT = LINEAR_PCM_8KHZ_16BITS_SIGNED_MONO_LITTLE_ENDIAN;

        DataFormat(String shortAlias, String description) {
            this.shortAlias = shortAlias;
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public String getShortAlias() { return shortAlias; }

        public static DataFormat fromShortAlias(String shortAlias) {
            for (DataFormat df : DataFormat.values()) {
                if (df.shortAlias.equals(shortAlias)) return df;
            }
            return null;
        }

    }

    default DataFormat dataProduced() {
        return DataFormat.DEFAULT;
    }

    /**
     * read raw data linear PCM 8kHz, 16 bits signed, mono-channel, little endian
     * @return
     */
    byte[] readData();

    default boolean finished() { return false; }

    default void waitFinished() throws InterruptedException {
        throw new RuntimeException("Waiting for finished not supported");
    }

}
