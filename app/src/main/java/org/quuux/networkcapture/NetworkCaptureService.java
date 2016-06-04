package org.quuux.networkcapture;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

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

    private void inspectPacket(final ByteBuffer packet) {
        int version = IPv4Header.getVersion(packet);
        int headerWords = IPv4Header.getIHL(packet);
        int len = IPv4Header.getLength(packet);
        int ttl = IPv4Header.getTTL(packet);

        byte[] src = new byte[4];
        IPv4Header.getSource(packet, src);

        byte[] dest = new byte[4];
        IPv4Header.getDest(packet, dest);

        Log.d(TAG, "IP: " + Util.toHexString(packet.array(), packet.limit()));
        Log.d(TAG, String.format("IP: version=%s / headerWords=%s / len=%s / ttl=%s / protocol=%s / src=%s / dest=%s",
                version, headerWords, len, ttl, IPv4Header.getProtocolName(packet), IPv4Header.formatAddress(src), IPv4Header.formatAddress(dest)));

        inspectPayload(packet);
    }

    private void inspectPayload(final ByteBuffer header) {
        int protocol = IPv4Header.getProtocol(header);
        header.position(20);
        ByteBuffer packet = header.slice();
        header.position(0);
        if (protocol == TCPHeader.PROTOCOL_NUMBER)
            inspectTCPPacket(packet);
        else if (protocol == UDPHeader.PROTOCOL_NUMBER)
            inspectUDPPacket(packet);
    }

    private void inspectUDPPacket(final ByteBuffer packet) {
        Log.d(TAG, "UDP: " + Util.toHexString(packet.array(), packet.limit()));
        Log.d(TAG, String.format("UDP: src port=%s / dest port=%s / length = %s",
                UDPHeader.getSourcePort(packet), UDPHeader.getDestPort(packet), UDPHeader.getLength(packet)));
    }

    private void inspectTCPPacket(final ByteBuffer packet) {
        Log.d(TAG, "TCP: " + Util.toHexString(packet.array(), packet.limit()));
        Log.d(TAG, String.format("TCP: src port=%s / dest port=%s",
                TCPHeader.getSourcePort(packet), TCPHeader.getDestPort(packet)));
    }

    void readPacket(final FileInputStream in, final ByteBuffer packet) throws IOException {
        int length = in.read(packet.array(), 0, 65535);
        packet.limit(length);
        if (length > 0) {
            inspectPacket(packet);
        }
        packet.clear();
    }

    class CaptureThread extends Thread {

        boolean capturing;

        @Override
        public void run() {

            final ParcelFileDescriptor iface = configure();
            final FileInputStream in = new FileInputStream(iface.getFileDescriptor());
            final FileOutputStream out = new FileOutputStream(iface.getFileDescriptor());

            final ByteBuffer packet = ByteBuffer.allocate(65535);
            packet.order(ByteOrder.BIG_ENDIAN);

            capturing = true;
            while (capturing) {
                try {
                    readPacket(in, packet);
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
