package org.quuux.networkcapture;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import org.quuux.networkcapture.net.Forwarder;
import org.quuux.networkcapture.net.IPv4Packet;
import org.quuux.networkcapture.net.NetworkConnection;
import org.quuux.networkcapture.net.TCPForwarder;
import org.quuux.networkcapture.net.UDPForwarder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class NetworkCaptureService extends VpnService {

    private static final String TAG = "NetworkCaptureService";

    BlockingQueue<IPv4Packet> outbound = new LinkedBlockingQueue<>();

    CaptureThread captureThread;
    WriterThread writerThread;
    Map<NetworkConnection, Forwarder> forwarders = new HashMap<>();
    ParcelFileDescriptor iface;

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        iface = configure();

        captureThread = new CaptureThread(iface);
        captureThread.start();

        writerThread = new WriterThread(iface, outbound);
        writerThread.start();

        return START_STICKY;
    }

    private ParcelFileDescriptor configure() {
        return new VpnService.Builder()
                .addAddress("10.0.0.1", 24)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("8.8.8.8")
                .addDnsServer("8.8.4.4")
                .setSession("capture")
                .establish();
    }

    private void inspectPacket(final IPv4Packet packet) {
        Log.d(TAG, "HEX:" + packet.toHex());
        Log.d(TAG, packet.inspect());
    }

    private Forwarder createForwarder(final NetworkConnection nc) {
        Forwarder forwarder = null;
        if (nc.isTCP()) {
            forwarder = new TCPForwarder(outbound);
        } else if (nc.isUDP()) {
            forwarder = new UDPForwarder(outbound);
        }
        return forwarder;
    }

    private Forwarder getForwarder(final NetworkConnection nc) {
        Forwarder forwarder = forwarders.get(nc);
        if (forwarder == null) {
            forwarder = createForwarder(nc);

            try {
                forwarder.connect(nc.dest);
                forwarders.put(nc, forwarder);
            } catch (IOException e) {
                Log.e(TAG, "Error connecting forward", e);
                forwarder = null;
            }

        }
        return forwarder;
    }

    private void forwardPacket(final IPv4Packet packet) throws IOException {
        final NetworkConnection nc = NetworkConnection.fromPacket(packet);
        if (nc != null) {
            final Forwarder forwarder = getForwarder(nc);
            if (forwarder != null)
                forwarder.forward(packet);
        } else {
            Log.d(TAG, String.format("dropping unhandled packet for protocol = %s", packet.getProtocol()));
        }
    }

    void readPacket(final FileInputStream in, final IPv4Packet packet) throws IOException {
        final ByteBuffer buffer = packet.getBuffer();
        int length = in.read(buffer.array(), 0, buffer.capacity());
        buffer.limit(length);
        if (length > 0) {
            //inspectPacket(packet);
            forwardPacket(packet);
        }
        buffer.clear();
    }

    class CaptureThread extends Thread {

        private final ParcelFileDescriptor iface;
        boolean capturing;

        public CaptureThread(final ParcelFileDescriptor iface) {
            this.iface = iface;
        }

        @Override
        public void run() {

            final FileInputStream in = new FileInputStream(iface.getFileDescriptor());

            final ByteBuffer buffer = ByteBuffer.allocate(IPv4Packet.MAX_SIZE);
            buffer.order(ByteOrder.BIG_ENDIAN);

            final IPv4Packet IPv4Packet = new IPv4Packet(buffer);

            capturing = true;
            while (capturing) {
                try {
                    readPacket(in, IPv4Packet);
                } catch (IOException e) {
                    Log.e(TAG, "error reading input from interface", e);
                    capturing = false;
                }
            }

            try {
                iface.close();
            } catch (IOException e) {
                Log.e(TAG, "error closing interface", e);
            }
        }
    }

    class WriterThread extends Thread {

        private boolean writing;
        private final ParcelFileDescriptor iface;
        private final BlockingQueue<IPv4Packet> queue;

        public WriterThread(final ParcelFileDescriptor iface, final BlockingQueue<IPv4Packet> queue) {
            this.iface = iface;
            this.queue = queue;
        }

        @Override
        public void run() {

            final FileOutputStream out = new FileOutputStream(iface.getFileDescriptor());

            writing = true;
            while (writing) {
                try {
                    final IPv4Packet packet = queue.take();
                    out.write(packet.getBuffer().array(), 0, packet.getBuffer().limit());
                } catch (InterruptedException | IOException e) {
                    Log.e(TAG, "Error writing outbound packet", e);
                    writing = false;
                }
            }
        }
    }
}

