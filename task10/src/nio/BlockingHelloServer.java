package nio;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static nio.Monitoring.*;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class BlockingHelloServer implements Runnable {
    private static final AtomicInteger clients = new AtomicInteger();
    private static final AtomicInteger active = new AtomicInteger();

    private final int port;

    public BlockingHelloServer(final int port) {
        this.port = port;
    }

    @Override
    public void run() {
        io(() -> {
            try (final ServerSocket serverSocket = new ServerSocket(port, 1000)) {
                while (true) {
                    final Socket socket = serverSocket.accept();
                    clients.incrementAndGet();
                    active.incrementAndGet();
                    new Thread(() -> io(() -> {
                        try {
                            final HelloBuffer buffer = new HelloBuffer();
                            while (true) {
                                final Optional<String> request = buffer.receive(socket.getInputStream());
                                if (request.isEmpty()) {
                                    break;
                                }
//                                System.out.println("Sever:  " + request.get());
                                HelloBuffer.send(socket.getOutputStream(), "Hello, " + request.get());
                            }
                        } finally {
                            socket.close();
                            active.decrementAndGet();
                        }
                    })).start();
                }
            }
        });
    }

    public static void main(final String[] args) {
        monitor(
                indicator("clients", clients),
                indicator("active", active),
                indicator("responses", HelloBuffer.responses),
                indicator("received", HelloBuffer.received),
                indicator("sent", HelloBuffer.sent)
        );
        new BlockingHelloServer(Integer.parseInt(args[0])).run();
    }
}
