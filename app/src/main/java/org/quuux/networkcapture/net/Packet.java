package org.quuux.networkcapture.net;

import org.quuux.networkcapture.util.Util;

import java.nio.ByteBuffer;
import java.util.Arrays;

abstract public class Packet {

    ByteBuffer buffer;

    public Packet(final ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public ByteBuffer getPayload() {
        buffer.position(getHeaderSize());
        ByteBuffer payload = buffer.slice();
        buffer.position(0);
        return payload;
    }

    public abstract int getHeaderSize();

    public void clear() {
        buffer.clear();
        Arrays.fill(buffer.array(), (byte) 0);
    }

    public String toHex() {
        return Util.toHexString(buffer.array(), buffer.limit());
    }

    public abstract String inspect();

}
