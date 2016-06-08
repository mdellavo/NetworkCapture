package org.quuux.networkcapture;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import org.quuux.networkcapture.net.IPv4Packet;

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
        Log.d(TAG, "HEX:" + packet.toHex());
        Log.d(TAG, packet.inspect());
        Log.d(TAG, packet.getPayloadPacket().inspect());
    }

    private void forwardPacket(final IPv4Packet packet) {

    }

    void readPacket(final FileInputStream in, final IPv4Packet packet) throws IOException {
        final ByteBuffer buffer = packet.getBuffer();
        int length = in.read(buffer.array(), 0, buffer.capacity());
        buffer.limit(length);
        if (length > 0) {
            inspectPacket(packet);
            forwardPacket(packet);
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
