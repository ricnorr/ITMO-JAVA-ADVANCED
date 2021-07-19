package nio;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class Channels {
    private static Path file;

    public static void main(final String[] args) throws IOException {
        prepare();
        try {

            System.out.println(file);
            readableByteChannel();
            scatteringByteChannel();
            seekableByteChannel();
            memoryMappedFile();
        } finally {
            cleanup();
        }
    }

    private static void readableByteChannel() throws IOException {
        part("ReadableByteChannel");
        final ByteBuffer buffer = ByteBuffer.allocate(7);

        try (ReadableByteChannel channel = Files.newByteChannel(file)){
            dump(channel.read(buffer) + " bytes read", buffer);
            removeData(buffer);
            dump(channel.read(buffer) + " bytes read", buffer);
            removeData(buffer);
        }
    }

    private static void scatteringByteChannel() throws IOException {
        part("ScatteringByteChannel");
        final ByteBuffer buffer1 = ByteBuffer.allocate(7);
        final ByteBuffer buffer2 = ByteBuffer.allocate(7);

        try (ScatteringByteChannel channel = FileChannel.open(file)){
            System.out.println("    " + channel.read(new ByteBuffer[]{buffer1, buffer2}) + " bytes read");
            dump("Buffer 1", buffer1);
            dump("Buffer 2", buffer1);
            removeData(buffer1);
            removeData(buffer2);
        }
    }

    private static void seekableByteChannel() throws IOException {
        part("SeekableByteChannel");
        final ByteBuffer buffer = ByteBuffer.allocate(7);

        try (SeekableByteChannel channel = FileChannel.open(file)){
            dump(channel.read(buffer) + " bytes read", buffer);
            removeData(buffer);
            dump(channel.read(buffer) + " bytes read", buffer);
            removeData(buffer);
            channel.position(0);
            System.out.println(" Reposition to zero");
            dump(channel.read(buffer) + " bytes read", buffer);
            removeData(buffer);
        }
    }


    private static void memoryMappedFile() throws IOException {
        part("MemoryMappedFile");
        try (FileChannel channel = FileChannel.open(file)){
            final MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, 12);
            dump("isDirect", buffer.isDirect());
            getData(buffer);
        }
    }
    private static void dump(final String description, final Object obj) {
        System.out.format("    %-20s %s\n", description, obj);
    }

    private static void removeData(final ByteBuffer buffer) {
        buffer.flip();
        dump("Flip", buffer);

        getData(buffer);
    }

    private static void getData(final ByteBuffer buffer) {
        final byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        dump("Data: '" + new String(data, StandardCharsets.UTF_8) + "'", buffer);

        buffer.clear();
        dump("Clear", buffer);
    }

    private static void part(final String channel) {
        System.out.println("=== " + channel);
    }

    private static void prepare() throws IOException {
        file = Files.createTempFile(Channels.class.getName() + "-", ".tmp");
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("Hello, world");
        }
    }

    private static void cleanup() throws IOException {
//        Files.deleteIfExists(file);
    }
}
