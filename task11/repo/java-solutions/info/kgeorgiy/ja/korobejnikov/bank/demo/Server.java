package info.kgeorgiy.ja.korobejnikov.bank.demo;

import info.kgeorgiy.ja.korobejnikov.bank.rmi.Bank;
import info.kgeorgiy.ja.korobejnikov.bank.rmi.RemoteBank;

import java.rmi.*;
import java.rmi.server.*;
import java.net.*;

/**
 * Demo server of a bank.
 */
public final class Server {
    private final static int DEFAULT_PORT = 8090;

    public static void main(final String... args) {
        final int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        final Bank bank = new RemoteBank(port);
        try {
            UnicastRemoteObject.exportObject(bank, port);
            Naming.rebind("//localhost/bank", bank);
            System.out.println("Server started");
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (final MalformedURLException e) {
            System.out.println("Malformed URL");
        }
    }
}
