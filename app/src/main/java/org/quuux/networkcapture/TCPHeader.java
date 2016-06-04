package org.quuux.networkcapture;


import java.nio.ByteBuffer;

public class TCPHeader {
    public static final int PROTOCOL_NUMBER = 6;

    public static int getSourcePort(final ByteBuffer header) {
        return header.getShort(0) & 0xffff;
    }

    public static int getDestPort(final ByteBuffer header) {
        return header.getShort(2) & 0xffff;
    }

    public static int getSequenceNumber(final ByteBuffer header) {
        return header.getInt(4);
    }
}
