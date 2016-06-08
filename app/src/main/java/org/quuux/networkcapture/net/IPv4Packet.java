package org.quuux.networkcapture.net;


import org.quuux.networkcapture.Util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class IPv4Packet {

    ByteBuffer buffer;

    public IPv4Packet(final ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public ByteBuffer getPayload() {
        buffer.position(getHeaderSize());
        ByteBuffer payload = buffer.slice();
        buffer.position(0);
        return payload;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public int getVersion() {
        return buffer.get(0) >> 4;
    }

    public int getIHL() {
        return buffer.get(0) & 0x0f;
    }

    public int getTOS() {
        return buffer.get(1);
    }

    public int getLength() {
        return buffer.getShort(2);
    }

    public int getIdent() {
        return buffer.getShort(4);
    }

    public int getFlags() {
        return (buffer.get(6) >> 1) & 0x07;
    }

    public int getFragOffset() {
        return buffer.getShort(6) & 0x1fff;
    }

    public int getTTL() {
        return buffer.get(8);
    }

    public int getProtocol() {
        return buffer.get(9);
    }

    public int getChecksum() {
        return buffer.getShort(10);
    }

    public void getSource(final byte[] src) {
        src[0] = buffer.get(12);
        src[1] = buffer.get(13);
        src[2] = buffer.get(14);
        src[3] = buffer.get(15);
    }

    public void getDest(final byte[] dest) {
        dest[0] = buffer.get(16);
        dest[1] = buffer.get(17);
        dest[2] = buffer.get(18);
        dest[3] = buffer.get(19);
    }

    public String getProtocolName() {
        final int protocol = getProtocol();
        if (protocol == 6)
            return "tcp";
        else if (protocol == 17)
            return "udp";
        else if (protocol == 1)
            return "icmp";
        else
            return String.valueOf(protocol);
    }

    public String formatAddress(byte[] addr) {
        try {
            return InetAddress.getByAddress(addr).getHostAddress();
        } catch (UnknownHostException e) {
            return e.toString();
        }
    }

    public String toHex() {
        return Util.toHexString(buffer.array(), buffer.limit());
    }

    public int getHeaderSize() {
        return getIHL() * 4;
    }
}
