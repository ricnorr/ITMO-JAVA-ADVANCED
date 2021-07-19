package info.kgeorgiy.ja.korobejnikov.bank.test;


import info.kgeorgiy.ja.korobejnikov.bank.rmi.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Tests for {@link Bank}.
 */
public class MyTest {
    private final static int PORT = 8080;
    private static final String BASE_PASSPORT = "passport";
    private static final String BASE_SURNAME = "surname";
    private static final String BASE_NAME = "name";
    private static final String BASE_ACCOUNT = "account";
    private static final String FULL_BASE_ACCOUNT = "passport:account";
    private Bank bank;
    private final static String BANK_URL = "//localhost/bank";
    private final static String INVALID = "INVALID";
    private final static int THREAD_CNT = 20;
    private final static int BASE_DIFF = 100;

    @BeforeEach
    void beforeEach() throws RemoteException, MalformedURLException {
        bank = new RemoteBank(PORT);
        UnicastRemoteObject.exportObject(bank, PORT);
        Naming.rebind(BANK_URL, bank);
    }


    @Test
    @DisplayName("Getting not existing remote person = null")
    public void remotePersonNotExist() throws RemoteException {
        Assertions.assertNull(bank.getRemotePerson(INVALID));
    }

    @Test
    @DisplayName("Getting not existing local person = null")
    public void localPersonNotExist() throws RemoteException {
        Assertions.assertNull(bank.getLocalPerson(INVALID));
    }


    @Test
    @DisplayName("Creating not existing remote person != null")
    public void creatingPerson() throws RemoteException {
        Assertions.assertNotNull(createBasePerson());
        Assertions.assertNotNull(getBaseLocalPerson());
    }

    @Test
    @DisplayName("Local person is serializable")
    public void localPersonIsSerializable() throws RemoteException {
        createBasePerson();
        try (ByteArrayOutputStream outputByteArrayStream = new ByteArrayOutputStream()) {
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputByteArrayStream)) {
                objectOutputStream.writeObject(getBaseLocalPerson());
                try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(outputByteArrayStream.toByteArray()))) {
                    Person serializedPerson = (Person) objectInputStream.readObject();
                    Assertions.assertEquals(serializedPerson, getBaseLocalPerson());
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            Assertions.fail();
        }
    }

    @Test
    @DisplayName("Created person has correct credentials")
    public void correctCredentialsCreatingPerson() throws RemoteException {
        Assertions.assertTrue(checkBaseCredentials(createBasePerson()));
    }


    @Test
    @DisplayName("Getting not existing remote account = null")
    public void remoteAccountNotExist() throws RemoteException {
        createBasePerson();
        Assertions.assertNull(getBaseRemoteAccount());
    }

    @Test
    @DisplayName("Getting not existing local account = null")
    public void localAccountNotExist() throws RemoteException {
        createBasePerson();
        Assertions.assertNull(getLocalAccount());
    }


    @Test
    @DisplayName("New created person's account credentials")
    public void correctCreatingAccountCredentials() throws RemoteException {
        createBasePerson();
        createBaseAccount();
        Assertions.assertEquals(getBaseRemoteAccount(), new BankAccount(FULL_BASE_ACCOUNT));
    }

    @Test
    @DisplayName("New created (without person) account credentials")
    public void correctCreatingAccountInBankCredentials() throws RemoteException {
        createBasePerson();
        bank.createAccount(BASE_ACCOUNT);
        Assertions.assertEquals(bank.getAccount(BASE_ACCOUNT), new BankAccount(BASE_ACCOUNT));
    }


    @Test
    @DisplayName("Remote account changes effect on another remote account")
    public void remoteAccountAfterRemoteChange() throws RemoteException {
        createBasePerson();
        createBaseAccount();
        changeBaseRemoteAccount(BASE_DIFF);
        Assertions.assertEquals(getBaseRemoteAccountAmount(), BASE_DIFF);
    }

    @Test
    @DisplayName("Remote account changes does not effect on before created local account")
    public void localAccountAfterRemoteChange() throws RemoteException {
        createBasePerson();
        createBaseAccount();
        final Account localAccount = getLocalAccount();
        changeBaseRemoteAccount(BASE_DIFF);
        Assertions.assertEquals(localAccount.getAmount(), 0);
    }


    @Test
    @DisplayName("Local account changes does not effect on another remote account")
    public void remoteAccountAfterLocalChange() throws RemoteException {
        createBasePerson();
        createBaseAccount();
        getLocalAccount().setAmount(BASE_DIFF);
        Assertions.assertEquals(getBaseRemoteAccount().getAmount(), 0);
    }


    @Test
    @DisplayName("Multi thread adding persons")
    public void multiThreadAddingPerson() {
        final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_CNT);
        IntStream.range(0, THREAD_CNT * 2).forEach(i -> executorService.submit(() -> {
            try {
                bank.createPerson(BASE_NAME + i, BASE_SURNAME + i, BASE_PASSPORT + i);
            } catch (final RemoteException ignored) {
                throw new RuntimeException("Creating remote account failed");
            }
        }));
        shutdownExecutor(executorService);
        IntStream.range(0, THREAD_CNT * 2).forEach(i -> {
            try {
                Assertions.assertTrue(equals(
                        bank.getRemotePerson(BASE_PASSPORT + i),
                        new LocalPerson(BASE_NAME + i, BASE_SURNAME + i, BASE_PASSPORT + i)));
            } catch (final RemoteException exception) {
                throw new RuntimeException("Creating remote account failed");
            }
        });
    }

    @Test
    @DisplayName("Multi thread adding accounts")
    public void multiThreadAddingAccount() throws RemoteException {
        final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_CNT);
        final Person person = createBasePerson();
        IntStream.range(0, THREAD_CNT * 2).forEach(i -> executorService.submit(() -> {
            try {
                person.addAccount(BASE_ACCOUNT + i).setAmount(i);
            } catch (final RemoteException exception) {
                throw new RuntimeException("Creating remote account failed");
            }
        }));
        shutdownExecutor(executorService);
        IntStream.range(0, THREAD_CNT * 2).forEach(i -> {
            try {
                Assertions.assertEquals(person.getAccount(BASE_ACCOUNT + i).getAmount(), i);
            } catch (final RemoteException exception) {
                throw new RuntimeException("Getting remote account failed");
            }
        });
    }

    @Test
    @DisplayName("Multi thread adding amount to account")
    public void multiThreadAddingAmount() throws RemoteException {
        final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_CNT);
        createBasePerson();
        final Account account = createBaseAccount();
        int delta = 1;
        IntStream.range(0, THREAD_CNT * 2).forEach(i -> executorService.submit(() -> {
            try {
                account.addAmount(delta);
            } catch (final RemoteException exception) {
                throw new RuntimeException("Creating remote account failed");
            }
        }));
        shutdownExecutor(executorService);
        Assertions.assertEquals(account.getAmount(), THREAD_CNT * 2);
    }


    private void shutdownExecutor(final ExecutorService executorService) {
        executorService.shutdown();
        try {
            executorService.awaitTermination(THREAD_CNT * 2 * 500, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ignored) {
        }
    }


    private boolean checkBaseCredentials(final Person person) throws RemoteException {
        return person.getPassport().equals(BASE_PASSPORT)
                && person.getSurname().equals(BASE_SURNAME) && person.getName().equals(BASE_NAME);
    }

    private Person createBasePerson() throws RemoteException {
        return bank.createPerson(BASE_NAME, BASE_SURNAME, BASE_PASSPORT);
    }

    private int getBaseRemoteAccountAmount() throws RemoteException {
        return getBaseRemotePerson().getAccount(BASE_ACCOUNT).getAmount();
    }

    private Account getLocalAccount() throws RemoteException {
        return bank.getLocalPerson(BASE_PASSPORT).getAccount(BASE_ACCOUNT);
    }

    private Person getBaseLocalPerson() throws RemoteException {
        return bank.getLocalPerson(BASE_PASSPORT);
    }


    private Account getBaseRemoteAccount() throws RemoteException {
        return getBaseRemotePerson().getAccount(BASE_ACCOUNT);
    }


    private Person getBaseRemotePerson() throws RemoteException {
        return bank.getRemotePerson(BASE_PASSPORT);
    }

    private void changeBaseRemoteAccount(final int value) throws RemoteException {
        bank.getRemotePerson(BASE_PASSPORT).getAccount(BASE_ACCOUNT).setAmount(value);
    }

    private Account createBaseAccount() throws RemoteException {
        return bank.getRemotePerson(BASE_PASSPORT).addAccount(BASE_ACCOUNT);
    }

    private boolean equals(final Person a, final Person b) throws RemoteException {
        return a.getPassport().equals(b.getPassport())
                && a.getName().equals(b.getName())
                && a.getSurname().equals(b.getSurname());
    }

}
