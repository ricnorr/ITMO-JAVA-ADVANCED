package nio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class HelloBuffer {
    public static final AtomicInteger responses = new AtomicInteger();
    public static final AtomicLong sent = new AtomicLong();
    public static final AtomicLong received = new AtomicLong();

    private int total = 0;
    private byte[] bytes = new byte[10];

    public static void send(final OutputStream os, final String string) throws IOException {
        final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        os.write(bytes);
        os.write(0);
        os.flush();
        sent.addAndGet(bytes.length + 1);
    }

    public Optional<String> receive(final InputStream is) throws IOException {
        boolean first = total == 0;
        while (true) {
            final int read = is.read(bytes, total, bytes.length - total);
            if (read == -1) {
                if (first) {
                    return Optional.empty();
                }
                throw new IOException("Unexpected end of input");
            }
            received.addAndGet(read);
            first = false;

            for (int j = 0; j < read; j++) {
                if (bytes[total + j] == 0) {
                    final String result = new String(bytes, 0, total + j, StandardCharsets.UTF_8);
                    System.arraycopy(bytes, total + j + 1, bytes, 0, read - j - 1);
                    total = read - j - 1;
                    responses.incrementAndGet();
                    return Optional.of(result);
                }
            }
            total += read;
            if (total == bytes.length) {
                final byte[] newBytes = new byte[bytes.length * 2];
                System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
                bytes = newBytes;
            }
        }
    }
}
