package org.quuux.networkcapture.net;

import org.quuux.networkcapture.Util;

import java.nio.ByteBuffer;


public class UDPPacket {
    public static final int PROTOCOL_NUMBER = 17;
    private final ByteBuffer buffer;

    public UDPPacket(final ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public int getSourcePort() {
        return buffer.getShort(0);
    }

    public int getDestPort() {
        return buffer.getShort(2);
    }

    public int getLength() {
        return buffer.getShort(4);
    }

    public int getChecksum() {
        return buffer.getShort(6);
    }

    public int getHeaderSize() {
        return 4 * 2;
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
