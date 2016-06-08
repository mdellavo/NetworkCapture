package org.quuux.networkcapture.net;

import java.nio.ByteBuffer;


public class UDPPacket extends Packet {
    public static final int PROTOCOL_NUMBER = 17;

    public UDPPacket(final ByteBuffer buffer) {
        super(buffer);
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

    @Override
    public String inspect() {
        return String.format("UDP: src port=%s / dest port=%s / length = %s", getSourcePort(), getDestPort(), getLength());
    }
}
