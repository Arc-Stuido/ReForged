package net.minecraft.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record PngInfo(int width, int height) {
    private static final long PNG_HEADER = -8552249625308161526L;
    private static final int IHDR_TYPE = 1229472850;
    private static final int IHDR_SIZE = 13;

    public static PngInfo fromStream(InputStream p_301756_) throws IOException {
        DataInputStream datainputstream = new DataInputStream(p_301756_);
        if (datainputstream.readLong() != -8552249625308161526L) {
            throw new IOException("Bad PNG Signature");
        } else if (datainputstream.readInt() != 13) {
            throw new IOException("Bad length for IHDR chunk!");
        } else if (datainputstream.readInt() != 1229472850) {
            throw new IOException("Bad type for IHDR chunk!");
        } else {
            int i = datainputstream.readInt();
            int j = datainputstream.readInt();
            return new PngInfo(i, j);
        }
    }

    public static PngInfo fromBytes(byte[] p_301719_) throws IOException {
        return fromStream(new ByteArrayInputStream(p_301719_));
    }

    public static void validateHeader(ByteBuffer p_311156_) throws IOException {
        ByteOrder byteorder = p_311156_.order();
        p_311156_.order(ByteOrder.BIG_ENDIAN);
        if (p_311156_.getLong(0) != -8552249625308161526L) {
            throw new IOException("Bad PNG Signature");
        } else if (p_311156_.getInt(8) != 13) {
            throw new IOException("Bad length for IHDR chunk!");
        } else if (p_311156_.getInt(12) != 1229472850) {
            throw new IOException("Bad type for IHDR chunk!");
        } else {
            p_311156_.order(byteorder);
        }
    }
}