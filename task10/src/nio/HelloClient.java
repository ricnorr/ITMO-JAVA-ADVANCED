package nio;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import static nio.Monitoring.*;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class HelloClient implements Runnable {
    private static final AtomicInteger clients = new AtomicInteger();
    private static final AtomicInteger active = new AtomicInteger();

    private final String host;
    private final int port;
    private final String prefix;

    public HelloClient(final String host, final int port, final String prefix) {
        this.host = host;
        this.port = port;
        this.prefix = prefix;
    }

    public void run() {
        clients.incrementAndGet();
        active.incrementAndGet();
        io(() -> {
            try (final Socket socket = new Socket(host, port)) {
                final OutputStream os = socket.getOutputStream();
                final HelloBuffer buffer = new HelloBuffer();

                for (int i = 0; i < 10; i++) {
                    HelloBuffer.send(os, prefix + "_" + i);
                    final String response = buffer.receive(socket.getInputStream()).get();
//                    System.out.println("Client: " + response);
                    Thread.sleep(1000);
                }
                for (int i = 10; i < 20; i++) {
                    HelloBuffer.send(os, prefix + "_" + i);
                }
                for (int i = 10; i < 20; i++) {
                    final String response = buffer.receive(socket.getInputStream()).get();
//                System.out.println("Client: " + response);
                }
            } finally {
                active.decrementAndGet();
            }
        });
    }

    public static void main(final String[] args) throws IOException {
        monitor(
                indicator("clients", clients),
                indicator("active", active),
                indicator("responses", HelloBuffer.responses),
                indicator("received", HelloBuffer.received),
                indicator("sent", HelloBuffer.sent)
        );

        final int threads = Integer.parseInt(args[3]);
        final HelloClient client = new HelloClient(args[0], Integer.parseInt(args[1]), args[2]);
        for (int i = 0; i < threads; i++) {
            new Thread(client).start();
        }
    }
}
