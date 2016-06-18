package org.quuux.networkcapture.net;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

public class TCPForwarder implements Forwarder {
    public static final String TAG = "UDPForwarder";

    private final BlockingQueue<IPv4Packet> outbound;
    private Socket socket;
    private ReaderThread readerThread;

    public TCPForwarder(final BlockingQueue<IPv4Packet> outbound) {
        this.outbound = outbound;
    }

    public void connect(final SocketAddress addr) throws IOException {
        socket = new Socket();
        socket.connect(addr);
        readerThread = new ReaderThread();
        readerThread.start();
    }

    public void close() throws IOException {
        socket.close();
    }

    @Override
    public void forward(final IPv4Packet ipPacket) throws IOException {
        final TCPPacket packet = ipPacket.getTCPPayload();

        if (packet.isSyn()) {

        } else if (packet.isFin()) {

        } else if (packet.isRst()) {

        }

        final ByteBuffer payload = packet.getPayload();
        if (payload.limit() > 0)
            socket.getOutputStream().write(payload.array());
    }

    class ReaderThread extends Thread {
        boolean reading;

        @Override
        public void run() {
            reading = true;
            final byte[] buffer = new byte[IPv4Packet.MAX_SIZE];
            while (reading) {
                try {
                    final InputStream in = socket.getInputStream();
                    in.read(buffer);
                } catch (IOException e) {
                    Log.e(TAG, "error receiving udp packet", e);
                    reading = false;
                }
            }
        }
    }

}
