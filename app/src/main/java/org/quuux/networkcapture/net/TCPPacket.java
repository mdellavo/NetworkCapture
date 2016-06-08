package org.quuux.networkcapture.net;

import java.nio.ByteBuffer;

public class TCPPacket extends Packet{
    public static final int PROTOCOL_NUMBER = 6;

    public TCPPacket(final ByteBuffer buffer) {
        super(buffer);
    }

    public int getSourcePort() {
        return buffer.getShort(0) & 0xffff;
    }

    public int getDestPort() {
        return buffer.getShort(2) & 0xffff;
    }

    public long getSeqNumber() {
        return buffer.getInt(4);
    }

    public long getAckNumber() {
        return buffer.getInt(8);
    }

    public int getDataOffset() {
        return buffer.get(12) >> 4;
    }

    public int getHeaderSize() {
        return getDataOffset() * 4;
    }

    @Override
    public String inspect() {
        return String.format("TCP: src port=%s / dest port=%s", getSourcePort(), getDestPort());
    }

}
