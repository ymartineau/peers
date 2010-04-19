package net.sourceforge.peers.rtp;


public class RtpPacket {

    private int version;
    private boolean padding;
    private boolean extension;
    private int csrcCount;
    private boolean marker;
    private int payloadType;
    private int sequenceNumber;
    private long timestamp;
    private long ssrc;
    private long[] csrcList;
    private byte[] data;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isPadding() {
        return padding;
    }

    public void setPadding(boolean padding) {
        this.padding = padding;
    }

    public boolean isExtension() {
        return extension;
    }

    public void setExtension(boolean extension) {
        this.extension = extension;
    }

    public int getCsrcCount() {
        return csrcCount;
    }

    public void setCsrcCount(int csrcCount) {
        this.csrcCount = csrcCount;
    }

    public boolean isMarker() {
        return marker;
    }

    public void setMarker(boolean marker) {
        this.marker = marker;
    }

    public int getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(int payloadType) {
        this.payloadType = payloadType;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getSsrc() {
        return ssrc;
    }

    public void setSsrc(long ssrc) {
        this.ssrc = ssrc;
    }

    public long[] getCsrcList() {
        return csrcList;
    }

    public void setCsrcList(long[] csrcList) {
        this.csrcList = csrcList;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}
