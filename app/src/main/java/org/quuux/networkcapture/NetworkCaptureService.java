package org.quuux.networkcapture;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import org.quuux.networkcapture.net.IPv4Packet;
import org.quuux.networkcapture.net.TCPPacket;
import org.quuux.networkcapture.net.UDPPacket;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class NetworkCaptureService extends VpnService {

    private static final String TAG = "NetworkCaptureService";

    CaptureThread workerThread;

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        workerThread = new CaptureThread();
        workerThread.start();

        return START_STICKY;
    }

    private ParcelFileDescriptor configure() {
        return new VpnService.Builder()
                .addAddress("10.0.0.1", 24)
                .addRoute("0.0.0.0", 0)
                .setSession("capture")
                .establish();
    }

    private void inspectPacket(final IPv4Packet packet) {
        int version = packet.getVersion();
        int headerWords = packet.getIHL();
        int len = packet.getLength();
        int ttl = packet.getTTL();

        byte[] src = new byte[4];
        packet.getSource(src);

        byte[] dest = new byte[4];
        packet.getDest(dest);

        Log.d(TAG, "IP: " + packet.toHex());
        Log.d(TAG, String.format("IP: version=%s / headerWords=%s / len=%s / ttl=%s / protocol=%s / src=%s / dest=%s",
                version, headerWords, len, ttl, packet.getProtocolName(), packet.formatAddress(src), packet.formatAddress(dest)));

        inspectPayload(packet);
    }

    private void inspectPayload(final IPv4Packet packet) {
        int protocol = packet.getProtocol();
        ByteBuffer payload = packet.getPayload();
        if (protocol == TCPPacket.PROTOCOL_NUMBER)
            inspectTCPPacket(new TCPPacket(payload));
        else if (protocol == UDPPacket.PROTOCOL_NUMBER)
            inspectUDPPacket(new UDPPacket(payload));
    }

    private void inspectUDPPacket(final UDPPacket packet) {
        Log.d(TAG, "UDP: " + packet.toHex());
        Log.d(TAG, String.format("UDP: src port=%s / dest port=%s / length = %s", packet.getSourcePort(), packet.getDestPort(), packet.getLength()));
    }

    private void inspectTCPPacket(final TCPPacket packet) {
        Log.d(TAG, "TCP: " + packet.toHex());
        Log.d(TAG, String.format("TCP: src port=%s / dest port=%s", packet.getSourcePort(), packet.getDestPort()));
    }

    void readPacket(final FileInputStream in, final IPv4Packet packet) throws IOException {
        final ByteBuffer buffer = packet.getBuffer();
        int length = in.read(buffer.array(), 0, buffer.capacity());
        buffer.limit(length);
        if (length > 0) {
            inspectPacket(packet);
        }
        buffer.clear();
    }

    class CaptureThread extends Thread {

        boolean capturing;

        @Override
        public void run() {

            final ParcelFileDescriptor iface = configure();
            final FileInputStream in = new FileInputStream(iface.getFileDescriptor());
            final FileOutputStream out = new FileOutputStream(iface.getFileDescriptor());

            final ByteBuffer buffer = ByteBuffer.allocate(65535);
            buffer.order(ByteOrder.BIG_ENDIAN);

            final IPv4Packet IPv4Packet = new IPv4Packet(buffer);

            capturing = true;
            while (capturing) {
                try {
                    readPacket(in, IPv4Packet);
                } catch (IOException e) {
                    Log.e(TAG, "error reading input from interface", e);
                }
            }

            try {
                iface.close();
            } catch (IOException e) {
                Log.e(TAG, "error closing interface", e);
            }
        }
    }
}
