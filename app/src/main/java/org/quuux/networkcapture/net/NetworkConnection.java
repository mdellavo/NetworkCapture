package org.quuux.networkcapture.net;

import java.net.InetSocketAddress;

public class NetworkConnection {
    public final int protocol;
    public final InetSocketAddress src, dest;

    public NetworkConnection(final int protocol, final InetSocketAddress src, final InetSocketAddress dest) {
        this.protocol = protocol;
        this.src = src;
        this.dest = dest;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof NetworkConnection))
            return false;
        final NetworkConnection other = (NetworkConnection) o;
        return protocol == other.protocol && src.equals(other) && dest.equals(other);
    }

    @Override
    public int hashCode() {
        return 7 * protocol * src.hashCode() * dest.hashCode();
    }

    public String format() {
        return String.format("%s:%s -> %s:%s", src.getHostName(), src.getPort(), dest.getHostName(), dest.getPort());
    }

    public boolean isTCP() {
        return protocol == TCPPacket.PROTOCOL_NUMBER;
    }

    public boolean isUDP() {
        return protocol == UDPPacket.PROTOCOL_NUMBER;
    }

    public static NetworkConnection fromPacket(final IPv4Packet ipPacket) {
        NetworkConnection nc = null;

        if (ipPacket.isTCP()) {
            final TCPPacket packet = ipPacket.getTCPPayload();
            final InetSocketAddress src = new InetSocketAddress(ipPacket.getSourceAddress(), packet.getSourcePort());
            final InetSocketAddress dest = new InetSocketAddress(ipPacket.getDestAddress(), packet.getDestPort());
            nc = new NetworkConnection(ipPacket.getProtocol(), src, dest);
        } else if (ipPacket.isUDP()) {
            final UDPPacket packet = ipPacket.getUDPPayload();
            final InetSocketAddress src = new InetSocketAddress(ipPacket.getSourceAddress(), packet.getSourcePort());
            final InetSocketAddress dest = new InetSocketAddress(ipPacket.getDestAddress(), packet.getDestPort());
            nc = new NetworkConnection(ipPacket.getProtocol(), src, dest);
        }

        return nc;
    }
}