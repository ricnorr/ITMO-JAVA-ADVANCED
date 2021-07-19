package info.kgeorgiy.ja.korobejnikov.bank.rmi;

import java.rmi.RemoteException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Changes with remote person (for example adding {@link Account}) are seen to other remote persons.
 */
public class RemotePerson extends AbstractPerson {
    private final Bank bank;

    /**
     * Creates remote person.
     *
     * @param name     person's name
     * @param surname  person's surname
     * @param passport person's passport
     */
    public RemotePerson(final String name, final String surname, final String passport, final Bank bank) {
        super(name, surname, passport, new ConcurrentHashMap<>());
        this.bank = bank;
    }

    @Override
    public Account addAccount(final String subId) throws RemoteException {
        final Account remoteAccount = bank.createAccount(bank.createAccountFullID(this, subId));
        return accountMap.computeIfAbsent(subId, x -> (BankAccount) remoteAccount);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof RemotePerson) {
            final RemotePerson person = (RemotePerson) obj;
            return person.getName().equals(name)
                    && person.getSurname().equals(surname)
                    && person.getPassport().equals(passport);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(passport, name, surname);
    }
}
