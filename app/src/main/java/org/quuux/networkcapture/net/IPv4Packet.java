package org.quuux.networkcapture.net;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class IPv4Packet extends Packet {

    public IPv4Packet(final ByteBuffer buffer) {
        super(buffer);
    }

    public int getHeaderSize() {
        return getIHL() * 4;
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
