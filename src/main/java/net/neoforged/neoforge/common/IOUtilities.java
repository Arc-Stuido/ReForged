package net.neoforged.neoforge.common;

import java.io.*;
import java.nio.file.Path;

/**
 * Stub: IO utilities for atomic file operations.
 */
public class IOUtilities {
    private IOUtilities() {}

    public static void atomicWrite(Path file, byte[] data) throws IOException {
        java.nio.file.Files.write(file, data);
    }
}
