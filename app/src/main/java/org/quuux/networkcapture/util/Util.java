package org.quuux.networkcapture.util;

public class Util {
    public static String toHexString(byte[] bytes, int limit) {
        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < limit; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public static String toHexString(byte[] bytes) {
        return toHexString(bytes, bytes.length);
    }
}
