package net.neoforged.neoforge.common.util;

/**
 * Stub: Hex dumper utility for debugging binary data.
 */
public class HexDumper {
    private HexDumper() {}

    public static String dump(byte[] data) {
        return dump(data, 0, data.length);
    }

    public static String dump(byte[] data, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < offset + length; i++) {
            sb.append(String.format("%02x ", data[i]));
            if ((i - offset + 1) % 16 == 0) sb.append('\n');
        }
        return sb.toString();
    }
}
