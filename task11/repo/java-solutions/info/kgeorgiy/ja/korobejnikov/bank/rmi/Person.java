package info.kgeorgiy.ja.korobejnikov.bank.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Person extends Remote {

    /**
     * Return person's name
     * @return name
     * @throws RemoteException if remote method call is failed
     */
    String getName() throws RemoteException;

    /**
     * Return person's surname
     * @return surname
     * @throws RemoteException if remote method call is failed
     */
    String getSurname() throws RemoteException;

    /**
     * Return person's passport id
     * @return passport id
     * @throws RemoteException if remote method call is failed
     */
    String getPassport() throws RemoteException;

    /**
     * Returns account by id.
     * @param subId account's subId
     * @return account by id
     * @throws RemoteException if remote method call if failed
     */
    Account getAccount(String subId) throws RemoteException;

    /**
     * Creates account by id.
     * If the person has such account, returns null.
     * @param subId account's subId
     * @return account by id.
     * @throws RemoteException if remote method call if failed
     */
    Account addAccount(String subId) throws RemoteException;

}
