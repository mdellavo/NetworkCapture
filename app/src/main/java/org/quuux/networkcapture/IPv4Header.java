package org.quuux.networkcapture;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class IPv4Header {
    public static int getVersion(final ByteBuffer header) {
        return header.get(0) >> 4;
    }

    public static int getIHL(final ByteBuffer header) {
        return header.get(0) & 0x0f;
    }

    public static int getTOS(final ByteBuffer header) {
        return header.get(1);
    }

    public static int getLength(final ByteBuffer header) {
        return header.getShort(2);
    }

    public static int getIdent(final ByteBuffer header) {
        return header.getShort(4);
    }

    public static int getFlags(final ByteBuffer header) {
        return (header.get(6) >> 1) & 0x07;
    }

    public static int getFragOffset(final ByteBuffer header) {
        return header.getShort(6) & 0x1fff;
    }

    public static int getTTL(final ByteBuffer header) {
        return header.get(8);
    }

    public static int getProtocol(final ByteBuffer header) {
        return header.get(9);
    }

    public static int getChecksum(final ByteBuffer header) {
        return header.getShort(10);
    }

    public static void getSource(final ByteBuffer header, final byte[] src) {
        src[0] = header.get(12);
        src[1] = header.get(13);
        src[2] = header.get(14);
        src[3] = header.get(15);
    }

    public static void getDest(final ByteBuffer header, final byte[] dest) {
        dest[0] = header.get(16);
        dest[1] = header.get(17);
        dest[2] = header.get(18);
        dest[3] = header.get(19);
    }

    static String getProtocolName(final ByteBuffer header) {
        final int protocol = getProtocol(header);
        if (protocol == 6)
            return "tcp";
        else if (protocol == 17)
            return "udp";
        else if (protocol == 1)
            return "icmp";
        else
            return String.valueOf(protocol);
    }

    static String formatAddress(byte[] addr) {
        try {
            return InetAddress.getByAddress(addr).getHostAddress();
        } catch (UnknownHostException e) {
            return e.toString();
        }
    }
}
