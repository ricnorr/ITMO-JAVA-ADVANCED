package info.kgeorgiy.ja.korobejnikov.bank.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {

    /**
     * Creates {@link Person} by parameters.
     * If person's with such passport id exists, returns null and creates nothing.
     *
     * @param name     person's name
     * @param surname  person's surname
     * @param passport person's passport
     * @return created {@link Person}
     * @throws RemoteException if remote method call failed.
     */
    Person createPerson(String name, String surname, String passport) throws RemoteException;

    /**
     * Returns {@link RemotePerson} found by passport.
     *
     * @param passport passport id.
     * @return found {@link Person}
     * @throws RemoteException if remote method call failed.
     */
    Person getRemotePerson(String passport) throws RemoteException;

    /**
     * Returns {@link LocalPerson} found by passport.
     *
     * @param passport passport id.
     * @return found {@link LocalPerson}
     * @throws RemoteException if remote method call failed.
     */
    Person getLocalPerson(String passport) throws RemoteException;

    /**
     * Creates remote account.
     *
     * @param id account's id
     * @return created account
     * @throws RemoteException if remote method call failed.
     */
    Account createAccount(final String id) throws RemoteException;


    /**
     * Returns remote account found by id.
     *
     * @param id account's id.
     * @return account
     * @throws RemoteException if remote method call failed.
     */
    Account getAccount(final String id) throws RemoteException;


    /**
     * Creates full id for account by subId.
     *
     * @param subId account's subId.
     * @return full id
     */
    String createAccountFullID(final Person person, final String subId) throws RemoteException;

}
