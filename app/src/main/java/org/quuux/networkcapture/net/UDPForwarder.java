package org.quuux.networkcapture.net;

import android.util.Log;

import org.quuux.networkcapture.util.Util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;


public class UDPForwarder implements Forwarder {

    public static final String TAG = "UDPForwarder";

    private final BlockingQueue<IPv4Packet> outbound;
    private SocketAddress addr;
    public DatagramSocket socket;
    private ReaderThread readerThread;

    public UDPForwarder(final InetAddress upstream, final BlockingQueue<IPv4Packet> outbound) {
        this.outbound = outbound;
        try {
            socket = new DatagramSocket(null);
            socket.bind(new InetSocketAddress(upstream, 0));
            Log.d(TAG, String.format("upd forwarder bound to %s:%s", socket.getLocalAddress(), socket.getLocalPort()));
        } catch (SocketException e) {
            Log.e(TAG, "unable to create datagram socket", e);
        }
    }

    public void connect(final SocketAddress addr) throws IOException {
        this.addr = addr;
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
        int len = packet.getLength() - packet.getHeaderSize();
        byte[] buffer = packet.getPayload().array();

        Log.d(TAG, String.format("forwarding %s bytes over udp to %s", len, addr));
        Log.d(TAG, String.format("udp payload: %s", Util.toHexString(buffer, len)));

        socket.send(new DatagramPacket(buffer, len, addr));
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
                    Log.d(TAG, String.format("reading udp from %s...", addr));
                    socket.receive(packet);
                    Log.d(TAG, String.format("received UDP packet %s", packet));
                } catch (IOException e) {
                    Log.e(TAG, "error receiving udp packet", e);
                    reading = false;
                }
            }
        }
    }
}
