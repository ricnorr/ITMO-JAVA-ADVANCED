package info.kgeorgiy.ja.korobejnikov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class HelloUDPServer implements HelloServer {

    private static final byte[] prefixStringInBytes = "Hello, ".getBytes(StandardCharsets.UTF_8);
    private DatagramSocket datagramSocket;
    private ExecutorService executorService;

    /***
     * Starts UDP server.
     * @param port server port.
     * @param threads number of working threads.
     */
    @Override
    public void start(final int port, final int threads) {
        executorService = Executors.newFixedThreadPool(threads);
        try {
            datagramSocket = new DatagramSocket(port);
        } catch (final SocketException e) {
            System.err.println("Can't create datagramSocket on port <" + port + ">");
            return;
        }
        IntStream.range(0, threads).forEach(index -> executorService.submit(this::listenSocket));

    }

    private void listenSocket() {
        final byte[] byteBuffer;
        try {

            byteBuffer = new byte[datagramSocket.getReceiveBufferSize()];
        } catch (final SocketException e) {
            System.err.println("Can't get size of socket's buffer: " + e.getMessage());
            return;
        }
        while (!datagramSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
            try {
                final DatagramPacket receivePacket = new DatagramPacket(byteBuffer, 0, byteBuffer.length);
                datagramSocket.receive(receivePacket);
                sendDatagram(datagramSocket, receivePacket);
            } catch (final IOException e) {
                System.err.println("Receiving datagram failed: " + e.getMessage());
            }
        }
    }

    private void sendDatagram(final DatagramSocket datagramSocket, final DatagramPacket receivePacket) {
        final byte[] responseByteArray = new byte[receivePacket.getLength() + prefixStringInBytes.length];
        System.arraycopy(prefixStringInBytes, 0, responseByteArray, 0, prefixStringInBytes.length);
        System.arraycopy(receivePacket.getData(), receivePacket.getOffset(),
                responseByteArray, prefixStringInBytes.length, receivePacket.getLength());
        receivePacket.setData(responseByteArray, 0, responseByteArray.length);
        try {
            datagramSocket.send(new DatagramPacket(responseByteArray, 0, responseByteArray.length,
                    receivePacket.getAddress(), receivePacket.getPort()));
        } catch (final IOException e) {
            System.err.println("Sending datagram failed: " + e.getMessage());
        }
    }

    /***
     * Closes server.
     */
    @Override
    public void close() {
        if (datagramSocket != null) {
            datagramSocket.close();
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(500, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ignored) {
        }
    }

    /**
     * Console wrap for {@code HelloUDPServer}.
     * Argument format is {@code [port, threads]}.
     *
     * @param args console arguments
     */
    public static void main(final String[] args) {
        if (args == null || args.length != 2 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Illegal arguments format. Should be <port> <threads>");
            return;
        }
        final int port;
        final int threads;
        try {
            port = Integer.parseInt(args[0]);
            threads = Integer.parseInt(args[1]);
        } catch (final NumberFormatException exception) {
            System.err.println("Arguments must be integers");
            return;
        }
        final HelloUDPServer server = new HelloUDPServer();
        server.start(port, threads);
    }
}
