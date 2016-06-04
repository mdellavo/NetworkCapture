package org.quuux.networkcapture;

import java.nio.ByteBuffer;


public class UDPHeader {
    public static final int PROTOCOL_NUMBER = 17;

    public static int getSourcePort(final ByteBuffer header) {
        return header.getShort(0);
    }

    public static int getDestPort(final ByteBuffer header) {
        return header.getShort(2);
    }

    public static int getLength(final ByteBuffer header) {
        return header.getShort(4);
    }

    public static int getChecksum(final ByteBuffer header) {
        return header.getShort(6);
    }
}
