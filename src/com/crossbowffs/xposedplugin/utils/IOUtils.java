package com.crossbowffs.xposedplugin.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class IOUtils {
    private IOUtils() { }

    public static void copyStreams(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int count = in.read(buffer);
        while (count >= 0) {
            out.write(buffer, 0, count);
            count = in.read(buffer);
        }
    }

    public static void silentlyClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}
