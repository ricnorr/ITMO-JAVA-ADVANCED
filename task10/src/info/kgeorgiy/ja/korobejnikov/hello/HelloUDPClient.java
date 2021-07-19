package info.kgeorgiy.ja.korobejnikov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class HelloUDPClient implements HelloClient {

    private static final int SOCKET_TIMEOUT_MS = 500;
    private static final int PORT_MX = 65535;
    private static final String ARGS_FORMAT = "Correct format is [host] [port] [prefix] [threads] [requests].";
    /**
     * Runs {@code HelloUDPClient}
     *
     * @param host     server host
     * @param port     server port
     * @param prefix   request prefix
     * @param threads  number of request threads
     * @param requests number of requests per thread.
     */
    @Override
    public void run(final String host, final int port, final String prefix, final int threads, final int requests) {
        final ExecutorService executorService = Executors.newFixedThreadPool(threads);
        final SocketAddress inetAddress;
        inetAddress = new InetSocketAddress(host, port);
        IntStream.range(0, threads).forEach(index -> executorService.submit(() -> doRequest(index, requests, prefix, inetAddress)));
        executorService.shutdown();
        try {
            executorService.awaitTermination(threads * requests * 500 * 10L, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ignored) {
        }
    }


    private boolean receivePacket(final DatagramSocket datagramSocket, final DatagramPacket datagramPacket,
                                  final String request, final byte[] responseStorage) {
        try {
            datagramPacket.setData(responseStorage, 0, responseStorage.length);
            datagramSocket.receive(datagramPacket);
            final String responseString = new String(
                    datagramPacket.getData(),
                    0,
                    datagramPacket.getLength(),
                    StandardCharsets.UTF_8);
            if (responseString.contains(request)) {
                System.out.println(responseString);
                return true;
            }
        } catch (final IOException ignored) {

        }
        return false;
    }


    private void doRequest(final int threadIndex, final int requests, final String prefix, final SocketAddress inetAddress) {
        try (final DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.setSoTimeout(SOCKET_TIMEOUT_MS);
            final byte[] responseStorage = new byte[datagramSocket.getReceiveBufferSize()];
            for (int j = 0; j < requests; j++) {
                final String request = (prefix + threadIndex + "_" + j);
                final byte[] requestInBytes = request.getBytes(StandardCharsets.UTF_8);
                System.out.println(request);
                while (true) {
                    try {
                        final DatagramPacket datagramPacket = new DatagramPacket(
                                requestInBytes,
                                requestInBytes.length,
                                inetAddress);
                        datagramSocket.send(datagramPacket);
                        if (receivePacket(datagramSocket, datagramPacket, request, responseStorage)) {
                            break;
                        }
                    } catch (final IOException ignored) {
                    }
                }
            }
        } catch (final SocketException e) {
            System.err.println("Socket problems: " + e.getMessage());
        }
    }


    /**
     * Console wrap for {@code HelloUDPClient}.
     * Console arguments format is {@code [host port prefix threads requests]}.
     *
     * @param args console arguments
     */
    public static void main(final String[] args) {
        if (args == null || args.length != 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Illegal arguments format. " + ARGS_FORMAT);
            return;
        }
        final int port;
        final int threads;
        final int requests;
        try {
            port = Integer.parseInt(args[1]);
            threads = Integer.parseInt(args[3]);
            requests = Integer.parseInt(args[4]);
        } catch (final NumberFormatException e) {
            System.err.println("Arguments 1, 3, 4 should be integers");
            return;
        }
        if (port > PORT_MX) {
            System.err.println("Port should be <= " + PORT_MX + ", but actually is <" + port + ">");
        }
        new HelloUDPClient().run(args[0], port, args[1], threads, requests);
    }

}



