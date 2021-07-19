package info.kgeorgiy.ja.korobejnikov.bank.rmi;

import java.io.UncheckedIOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteBank implements Bank {
    private final int port;
    private final Map<String, RemotePerson> persons = new ConcurrentHashMap<>();
    private final Map<String, BankAccount> accounts = new ConcurrentHashMap<>();

    /**
     * Creates bank on port.
     *
     * @param port port
     */
    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Account createAccount(final String id) throws RemoteException {
        return accounts.computeIfAbsent(id, x -> {
            final BankAccount account = new BankAccount(x);
            try {
                UnicastRemoteObject.exportObject(account, port);
            } catch (RemoteException exception) {
                throw new UncheckedIOException(exception);
            }
            return account;
        });
    }

    @Override
    public Account getAccount(final String id) {
        return accounts.get(id);
    }


    @Override
    public Person createPerson(final String name, final String surname, final String passport) throws RemoteException {
        return persons.computeIfAbsent(passport, id -> {
            final RemotePerson person = new RemotePerson(name, surname, passport, this);
            try {
                UnicastRemoteObject.exportObject(person, port);
            } catch (RemoteException exception) {
                throw new UncheckedIOException(exception);
            }
            return person;
        });
    }


    @Override
    public String createAccountFullID(Person person, String subId) throws RemoteException {
        return person.getPassport() + ":" + subId;
    }

    @Override
    public Person getRemotePerson(final String passport) {
        return persons.get(passport);
    }

    @Override
    public Person getLocalPerson(final String passport) {
        final RemotePerson person = persons.get(passport);
        return person == null
                ? null
                : new LocalPerson(person, LocalPerson.prepareLocalMap(person.accountMap));
    }


}
