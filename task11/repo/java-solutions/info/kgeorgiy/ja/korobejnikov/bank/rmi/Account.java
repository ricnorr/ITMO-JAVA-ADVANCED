package info.kgeorgiy.ja.korobejnikov.bank.rmi;

import java.rmi.*;

public interface Account extends Remote {
    /** Returns account identifier. */
    String getId() throws RemoteException;

    /** Returns amount of money at the account. */
    int getAmount() throws RemoteException;

    /** Adds amount of money to the account */
    void addAmount(int amount) throws RemoteException;

    /** Sets amount of money at the account. */
    void setAmount(int amount) throws RemoteException;
}