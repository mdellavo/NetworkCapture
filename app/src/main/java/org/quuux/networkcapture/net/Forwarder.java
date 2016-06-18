package org.quuux.networkcapture.net;

import java.io.IOException;
import java.net.SocketAddress;


public interface Forwarder {
    void connect(SocketAddress addr) throws IOException;

    void close() throws IOException;

    void forward(IPv4Packet packet) throws IOException;
}
