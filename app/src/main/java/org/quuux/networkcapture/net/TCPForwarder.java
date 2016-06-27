package org.quuux.networkcapture.net;

import android.util.Log;

import org.quuux.networkcapture.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

public class TCPForwarder implements Forwarder {
    public static final String TAG = "TCPForwarder";

    private final BlockingQueue<IPv4Packet> outbound;
    private SocketAddress addr;
    public Socket socket;
    private ReaderThread readerThread;

    public TCPForwarder(final InetAddress upstream, final BlockingQueue<IPv4Packet> outbound) {
        socket = new Socket();
        try {
            socket.bind(new InetSocketAddress(upstream, 0));
        } catch (IOException e) {
            Log.e(TAG, "unable to bind new tcp socket", e);
        }
        this.outbound = outbound;
    }

    public void connect(final SocketAddress addr) throws IOException {
        this.addr = addr;
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

        Log.d(TAG, String.format("TCP data: %s", Util.toHexString(packet.getBuffer().array(), packet.getHeaderSize())));

        // handle tcp state and forge a response
        if (packet.isSyn()) {

            if (packet.isAck()) {
                Log.d(TAG, String.format("connection established to %s", addr.toString()));
            } else {
                Log.d(TAG, String.format("new connection to %s", addr.toString()));
            }

        } else if (packet.isFin()) {
            Log.d(TAG, String.format("connection to %s is finished", addr.toString()));

        } else if (packet.isRst()) {
            Log.d(TAG, String.format("connection to %s is reset", addr.toString()));
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
