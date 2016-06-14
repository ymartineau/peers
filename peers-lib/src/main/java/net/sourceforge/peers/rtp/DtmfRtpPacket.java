package net.sourceforge.peers.rtp;

public class DtmfRtpPacket extends RtpPacket{
	
	/**
	 * The previous DtmfRtpPacket for the same DTMF event.
	 */
	private DtmfRtpPacket previousDtmfRtpPacket;

	public DtmfRtpPacket() {
		super();
 	}
	
 	
	public DtmfRtpPacket(DtmfRtpPacket previousDtmfRtpPacket) {
		super();
		this.previousDtmfRtpPacket = previousDtmfRtpPacket;
	}


	public DtmfRtpPacket getPreviousDtmfRtpPacket() {
		return previousDtmfRtpPacket;
	}


	public void setPreviousDtmfRtpPacket(DtmfRtpPacket previousDtmfRtpPacket) {
		this.previousDtmfRtpPacket = previousDtmfRtpPacket;
	}
  
	
}
