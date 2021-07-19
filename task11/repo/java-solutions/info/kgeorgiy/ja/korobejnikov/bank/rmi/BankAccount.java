package info.kgeorgiy.ja.korobejnikov.bank.rmi;

import java.io.Serializable;
import java.util.Objects;


public class BankAccount implements Account, Serializable {
    private final String id;
    private int amount;

    /**
     * Creates basic {@code AbstractAccount}.
     * Default amount is {@code 0}.
     *
     * @param id id
     */
    public BankAccount(final String id) {
        this.id = id;
        amount = 0;
    }

    /**
     * Creates {@code AbstractAccount} by {@code RemoteAccount}.
     *
     * @param account account with credentials
     */
    public BankAccount(final BankAccount account) {
        id = account.getId();
        amount = account.getAmount();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    synchronized public int getAmount() {
        return amount;
    }

    @Override
    synchronized public void setAmount(final int amount) {
        this.amount = amount;
    }

    @Override
    synchronized public void addAmount(int amount) {
        this.amount += amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BankAccount) {
            BankAccount bankAccount = (BankAccount)obj;
            return id.equals(bankAccount.getId()) && amount == bankAccount.getAmount();
        }
        return false;
    }
}
