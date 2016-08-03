package net.sourceforge.peers.media;


import net.sourceforge.peers.rtp.RFC4733;
import net.sourceforge.peers.rtp.RtpPacket;

public class DtmfDecoder {
    private DtmfEventHandler handler;
    private RFC4733.DTMFEvent lastDtmfEvent;
    private int lastDuration;
    private int lastVolume;

    public DtmfDecoder(DtmfEventHandler handler){
        this.handler = handler;
        lastDuration = -1;
    }

    public void processPacket(RtpPacket packet) {
        if(packet.getPayloadType() != RFC4733.PAYLOAD_TYPE_TELEPHONE_EVENT) {
            throw new RuntimeException("Decoder only supports payloads of type " + RFC4733.TELEPHONE_EVENT);
        }

        if((packet.getData() == null) || (packet.getData().length != 4)) {
            throw  new RuntimeException(("Only RFC4733 formatted DTMF supported (Unsupported datalength, " + packet.getData().length + " bytes)"));
        }

        if(((packet.getData()[1] >> 7) & 1) == 1) {
            //End bit is set
            int duration = (((packet.getData()[2] & 0xff) << 8) | (packet.getData()[1] & 0xff));
            if(duration != 0) {
                //RFC4733 states that events with zero duration must be ignored
                RFC4733.DTMFEvent dtmfEvent = RFC4733.DTMFEvent.fromValue(packet.getData()[0]);
                int volume = (packet.getData()[1] & 0x3f);
                if((duration != lastDuration) || (dtmfEvent != lastDtmfEvent) || (lastVolume != volume)) {
                    //The RFC states that the end packet SHOULD be sent a total of three times.
                    //It does not say MUST, so to be flexible, the DTMF event is acknowledged on the first end event,
                    //and further end events are ignored.
                    lastDtmfEvent = dtmfEvent;
                    lastDuration = duration;
                    lastVolume = volume;
                    handler.dtmfDetected(dtmfEvent, duration);
                }
            }
        } else {
            //Reset "same as last" detector
            lastDuration = -1;
        }

    }
}
