package info.kgeorgiy.ja.korobejnikov.bank.rmi;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Changes with local person (for example adding {@link Account}) are seen only to local copy.
 */
public class LocalPerson extends AbstractPerson {


    /**
     * Creates local person from existing person and map.
     *
     * @param person person to be copied.
     * @param map    map of accounts for person.
     */
    public LocalPerson(final RemotePerson person, Map<String, BankAccount> map) {
        super(person.getName(), person.getPassport(), person.getSurname(), map);
    }

    /**
     * Creates local person by credentials.
     *
     * @param name     name
     * @param surname  surname
     * @param passport passport
     */
    public LocalPerson(final String name, final String surname, final String passport) {
        super(name, surname, passport, new HashMap<>());
    }

    @Override
    public Account addAccount(final String subId) {
        final BankAccount bankAccount = new BankAccount(subId);
        final BankAccount account = accountMap.putIfAbsent(subId, bankAccount);
        return account == null ? bankAccount : account;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof LocalPerson) {
            final LocalPerson person = (LocalPerson) obj;
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

    public static Map<String, BankAccount> prepareLocalMap(Map<String, BankAccount> accountMap) {
        Map<String, BankAccount> resultMap = new HashMap<>();
        accountMap.forEach((a, b) -> resultMap.put(a, new BankAccount(b)));
        return resultMap;
    }
}
