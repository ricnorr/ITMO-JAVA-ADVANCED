package nio;

import java.nio.Buffer;
import java.nio.CharBuffer;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class Buffers {
    private static void dump(final String name, final Object value) {
        System.out.format("    %-10s %s\n", name, value);
    }
    public static void dumpBuffer(final String description, final Buffer buffer) {
//        System.out.println("hasArray:" + buffer.hasArray());
//        System.out.println("position:" + buffer.position());
        System.out.println(description);
        System.out.println("    " + buffer);
        dump("position", buffer.position());
        dump("limit", buffer.limit());
        dump("capacity", buffer.capacity());
        dump("remaining", buffer.remaining());
    }
    public static void main(final String[] args) {
        final CharBuffer buffer = CharBuffer.allocate(100);
        dumpBuffer("New buffer", buffer);

        buffer.put("Hello, world".toCharArray());
        dumpBuffer("12 bytes written", buffer);

        buffer.flip();
        dumpBuffer("Flipped", buffer);

        dumpBuffer("5 bytes read: " + read(buffer, 5), buffer);

        buffer.mark();
        dumpBuffer("Position marked", buffer);

        dumpBuffer("5 bytes read: " + read(buffer, 5), buffer);

        buffer.reset();
        dumpBuffer("Reset", buffer);

        buffer.rewind();
        dumpBuffer("Rewind", buffer);

        buffer.clear();
        dumpBuffer("Clear", buffer);
    }

    private static String read(final CharBuffer buffer, final int n) {
        final char[] data = new char[n];
        buffer.get(data);
        return new String(data);
    }
}
