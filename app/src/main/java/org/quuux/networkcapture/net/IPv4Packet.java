package org.quuux.networkcapture.net;

import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class IPv4Packet extends Packet {

    public static final String TAG = "IPv4Packet";
    public static final int MAX_SIZE = 65535;

    public IPv4Packet(final ByteBuffer buffer) {
        super(buffer);
    }

    public int getHeaderSize() {
        return getIHL() * 4;
    }

    public void setVersionAndIHL(int version, int bytes) {
        byte b = (byte) (((version << 4) & 0xff00) | (bytes & 0xff));
        buffer.put(0, b);
    }

    public void setVersionAndIHL(int version) {
        setVersionAndIHL(version, 5);
    }

    public void setVersionAndIHL() {
        setVersionAndIHL(4);
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

    public void setLength(short length) {
        buffer.putShort(2, length);
    }

    public int getLength() {
        return buffer.getShort(2);
    }

    public void setIdent(short ident) {
        buffer.putShort(4, ident);
    }

    public int getIdent() {
        return buffer.getShort(4);
    }

    public void setFlagsAndFragOffset(byte flags, short fragOffset) {
        short s = (short) (((flags & 0x1f) << 5) | (fragOffset & 0x1fff));
        buffer.putShort(6, s);
    }

    public int getFlags() {
        return (buffer.get(6) >> 1) & 0x07;
    }

    public int getFragOffset() {
        return buffer.getShort(6) & 0x1fff;
    }

    public void setTTL(byte ttl) {
        buffer.put(8, ttl);
    }

    public int getTTL() {
        return buffer.get(8);
    }

    public void setProtocol(byte protocol) {
        buffer.put(9, protocol);
    }

    public int getProtocol() {
        return buffer.get(9);
    }

    public void setChecksum() {
        buffer.putShort(10, (short) 0);
        long checksum = calculateChecksum(buffer.array(), getHeaderSize());
        buffer.putShort(10, (short) checksum);
    }

    public int getChecksum() {
        return buffer.getShort(10);
    }

    public void setSource(final byte[] src) {
        for (int i=0; i<4; i++)
            buffer.put(12 + i, src[i]);
    }

    public void getSource(final byte[] src) {
        for (int i=0; i<4; i++)
            src[i] = buffer.get(12 + i);
    }

    public InetAddress getSourceAddress() {
        final byte[] addr = new byte[4];
        getSource(addr);
        try {
            return InetAddress.getByAddress(addr);
        } catch (UnknownHostException e) {
            Log.e(TAG, "error getting source address");
        }
        return null;
    }

    public void setDest(final byte[] desg) {
        for (int i=0; i<4; i++)
            buffer.put(16 + i, desg[i]);
    }

    public void getDest(final byte[] dest) {
        for (int i=0; i<4; i++)
            dest[i] = buffer.get(16 + i);
    }

    public InetAddress getDestAddress() {
        final byte[] addr = new byte[4];
        getSource(addr);
        try {
            return InetAddress.getByAddress(addr);
        } catch (UnknownHostException e) {
            Log.e(TAG, "error getting dest address");
        }
        return null;
    }

    public boolean isTCP() {
        return getProtocol() == TCPPacket.PROTOCOL_NUMBER;
    }

    public boolean isUDP() {
        return getProtocol() == UDPPacket.PROTOCOL_NUMBER;
    }

    public String getProtocolName() {
        final int protocol = getProtocol();
        if (protocol == TCPPacket.PROTOCOL_NUMBER)
            return "tcp";
        else if (protocol == UDPPacket.PROTOCOL_NUMBER)
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

    public String getHostName(byte[] addr) {
        try {
            return InetAddress.getByAddress(addr).getCanonicalHostName();
        } catch (UnknownHostException e) {
            return e.toString();
        }
    }

    public UDPPacket getUDPPayload() {
        if (getProtocol() != UDPPacket.PROTOCOL_NUMBER)
            throw new RuntimeException("not a udp packet");

        return new UDPPacket(getPayload());
    }

    public TCPPacket getTCPPayload() {
        if (getProtocol() != TCPPacket.PROTOCOL_NUMBER)
            throw new RuntimeException("not a tcp packet");

        return new TCPPacket(getPayload());
    }

    public Packet getPayloadPacket() {
        int protocol = getProtocol();
        if (protocol == UDPPacket.PROTOCOL_NUMBER)
            return getUDPPayload();
        else if (protocol == TCPPacket.PROTOCOL_NUMBER)
            return getTCPPayload();

        throw new RuntimeException("unknown protocol");
    }

    @Override
    public String inspect() {
        int version = getVersion();
        int headerWords = getIHL();
        int len = getLength();
        int ttl = getTTL();

        byte[] src = new byte[4];
        getSource(src);

        byte[] dest = new byte[4];
        getDest(dest);

        return String.format("IP: %s -> %s / %s", formatAddress(src), formatAddress(dest), getPayloadPacket().inspect());
    }
}
