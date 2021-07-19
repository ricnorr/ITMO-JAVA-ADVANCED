package info.kgeorgiy.ja.korobejnikov.bank.rmi;

import java.io.Serializable;
import java.util.Map;

public abstract class AbstractPerson implements Person, Serializable {
    protected final String name;
    protected final String surname;
    protected final String passport;
    protected final Map<String, BankAccount> accountMap;

    /**
     * Creates {@code Abstract Person} by credentials.
     *
     * @param name     name
     * @param surname  surname
     * @param passport passport
     */
    public AbstractPerson(final String name, final String surname, final String passport, Map<String, BankAccount> accountMap) {
        this.name = name;
        this.surname = surname;
        this.passport = passport;
        this.accountMap = accountMap;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public String getPassport() {
        return passport;
    }

    @Override
    public Account getAccount(final String subId) {
        return accountMap.get(subId);
    }
}
