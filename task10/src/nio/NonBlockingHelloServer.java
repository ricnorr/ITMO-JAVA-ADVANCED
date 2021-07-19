package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static nio.Monitoring.*;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class NonBlockingHelloServer implements Runnable {
    private static final AtomicInteger clients = new AtomicInteger();
    private static final AtomicInteger active = new AtomicInteger();
    private static final AtomicInteger requests = new AtomicInteger();
    private static final AtomicLong received = new AtomicLong();
    private static final AtomicLong sent = new AtomicLong();

    private static class Client {
        private final Queue<ByteBuffer> output = new ArrayDeque<>();
        private byte[] bytes = new byte[10];
        private int total = 0;

        public Client() {
            active.incrementAndGet();
        }

        public void read(final int read, final ByteBuffer input) {
            if (read == -1) {
                active.decrementAndGet();
                return;
            }
            input.flip();
            for (int i = 0; i < input.limit(); i++) {
                append(input.get());
            }
            input.clear();
        }

        private void append(final byte b) {
            if (b == 0) {
                final String request = new String(bytes, 0, total);
                requests.incrementAndGet();
                final byte[] response = ("Hello, " + request).getBytes(StandardCharsets.UTF_8);
                send(response);
                send(new byte[1]);
                total = 0;
            } else {
                if (bytes.length == total) {
                    final byte[] newBytes = new byte[bytes.length * 2];
                    System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
                    bytes = newBytes;
                }
                bytes[total++] = b;
            }
        }

        private void send(final byte[] response) {
            output.add(ByteBuffer.wrap(response));
        }

        private ByteBuffer[] getOutput() {
            output.removeIf(b -> b.remaining() == 0);
            return output.toArray(new ByteBuffer[output.size()]);
        }
    }

    private final int port;

    public NonBlockingHelloServer(final int port) {
        this.port = port;
    }

    @Override
    public void run() {
        io(() -> {
            try (
                    final Selector selector = Selector.open();
                    final ServerSocketChannel serverChannel = ServerSocketChannel.open()
            ) {
                serverChannel.configureBlocking(false);
                serverChannel.bind(new InetSocketAddress(port), 1000);
                serverChannel.register(selector, SelectionKey.OP_ACCEPT);
                run(selector);
            }
        });
    }

    private static void run(final Selector selector) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(10);
        while (true) {
            selector.select(key -> io(() -> {
                if (key.isAcceptable()) {
                    final ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                    final SocketChannel socketChannel = serverChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ, new Client());
                    clients.incrementAndGet();
                }
                if (key.isReadable()) {
                    final SocketChannel socketChannel = (SocketChannel) key.channel();
                    final Client client = (Client) key.attachment();
                    try {
                        final int read = socketChannel.read(buffer);
                        if (read == -1) {
                            socketChannel.close();
                        } else {
                            received.addAndGet(read);
                        }
                        client.read(read, buffer);
                        final ByteBuffer[] output = client.getOutput();
                        if (output.length != 0) {
                            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        }
                    } catch (final IOException e) {
                        socketChannel.close();
                        exception(e);
                    }
                }
                if (key.isValid() && key.isWritable()) {
                    final SocketChannel socketChannel = (SocketChannel) key.channel();
                    final Client client = (Client) key.attachment();
                    final ByteBuffer[] output = client.getOutput();
                    if (output.length != 0) {
                        try {
                            final long write = socketChannel.write(output);
                            sent.addAndGet(write);
                        } catch (final IOException e) {
                            socketChannel.close();
                            exception(e);
                        }
                    } else {
                        key.interestOps(SelectionKey.OP_READ);
                    }
                }
            }));
        }
    }

    public static void main(final String[] args) throws IOException, InterruptedException {
        monitor(
                indicator("clients", clients),
                indicator("active", active),
                indicator("requests", requests),
                indicator("received", received),
                indicator("sent", sent)
        );
        new NonBlockingHelloServer(Integer.parseInt(args[0])).run();
    }
}
