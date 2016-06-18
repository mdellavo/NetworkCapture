package org.quuux.networkcapture.net;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.concurrent.BlockingQueue;


public class UDPForwarder implements Forwarder {

    public static final String TAG = "UDPForwarder";

    private final BlockingQueue<IPv4Packet> outbound;
    private DatagramSocket socket;
    private ReaderThread readerThread;

    public UDPForwarder(final BlockingQueue<IPv4Packet> outbound) {
        this.outbound = outbound;
    }

    public void connect(final SocketAddress addr) throws IOException {
        socket = new DatagramSocket();
        socket.connect(addr);
        readerThread = new ReaderThread();
        readerThread.start();
    }

    public void close() {
        socket.close();
    }

    @Override
    public void forward(final IPv4Packet ipPacket) throws IOException {
        final UDPPacket packet = ipPacket.getUDPPayload();
        socket.send(new DatagramPacket(packet.getPayload().array(), packet.getLength()));
    }

    class ReaderThread extends Thread {
        boolean reading;

        @Override
        public void run() {
            reading = true;
            final byte[] buffer = new byte[IPv4Packet.MAX_SIZE];
            final DatagramPacket packet = new DatagramPacket(buffer, IPv4Packet.MAX_SIZE);
            while (reading) {
                try {
                    socket.receive(packet);
                } catch (IOException e) {
                    Log.e(TAG, "error receiving udp packet", e);
                    reading = false;
                }
            }
        }
    }
}
