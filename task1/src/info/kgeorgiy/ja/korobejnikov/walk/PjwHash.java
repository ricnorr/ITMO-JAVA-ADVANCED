package info.kgeorgiy.ja.korobejnikov.walk;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class PjwHash {
    public static long fileToPjw(Path path) {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(path), 1024)) {
            long hash = 0;
            long high;
            int length;
            byte[] buffer = new byte[1024];
            while ((length = bufferedInputStream.read(buffer)) >= 0) {
                for (int i = 0; i < length; i++) {
                    int bit = buffer[i];
                    hash = (hash << 8) + (bit & 0xff);
                    if ((high = hash & 0xFF00_0000_0000_0000L) != 0) {
                        hash ^= high >> 48;
                        hash &= ~high;
                    }
                }
            }
            return hash;
        } catch (IOException e) {
            return 0;
        }
    }

    public static String concatHashPath(long hash, String filename) {
        return String.format("%016x", hash) + " " + filename + System.lineSeparator();
    }
}
