package org.quuux.networkcapture.net;


import org.quuux.networkcapture.Util;

import java.nio.ByteBuffer;

public class TCPPacket {
    public static final int PROTOCOL_NUMBER = 6;
    private final ByteBuffer buffer;

    public TCPPacket(final ByteBuffer buffer) {
        this.buffer = buffer;
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

    public ByteBuffer getPayload() {
        buffer.position(getHeaderSize());
        ByteBuffer payload = buffer.slice();
        buffer.position(0);
        return payload;
    }

    public String toHex() {
        return Util.toHexString(buffer.array(), buffer.limit());
    }
}
