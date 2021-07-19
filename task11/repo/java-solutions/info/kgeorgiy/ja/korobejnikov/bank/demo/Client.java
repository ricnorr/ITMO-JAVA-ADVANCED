package info.kgeorgiy.ja.korobejnikov.bank.demo;



import info.kgeorgiy.ja.korobejnikov.bank.rmi.Account;
import info.kgeorgiy.ja.korobejnikov.bank.rmi.Bank;
import info.kgeorgiy.ja.korobejnikov.bank.rmi.Person;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Demo client of a bank.
 */
public final class Client {
    private Client() {}

    public static void main(final String... args) throws RemoteException {
        final Bank bank;
        try {
            bank = (Bank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }

        final String name = args.length >= 1 ? args[0] : "user";
        final String surname = args.length >= 2 ? args[1] : "surname";
        final String passport = args.length >= 3 ? args[2] : "passport";
        final String accountId = args.length >= 4 ? args[3] : "account";
        final int difference = args.length >= 5 ? Integer.parseInt(args[4]) : 0;



        Person person = bank.getRemotePerson(passport);
        if (person == null) {
            System.out.println("Creating remote person.");
            person = bank.createPerson(name, surname, passport);
        } else {
            System.out.println("Person is already exist.");
        }
        if (!person.getName().equals(name) || !person.getSurname().equals(surname)) {
            System.out.println("Person not verified.");
            return;
        }
        System.out.println("Person is verified.");
        System.out.println("Person's name,surname, passport: " + person.getName() + " " + person.getSurname() + " " + person.getPassport());
        Account remoteAccount = person.addAccount(accountId);
        Person localPerson = bank.getLocalPerson(passport);
        Account localAccount = localPerson.getAccount(accountId);
        localAccount.setAmount(localAccount.getAmount() + difference);
        remoteAccount.setAmount(remoteAccount.getAmount() + 2 * difference);
        System.out.println("Money on remote after changing: " + remoteAccount.getAmount());
        System.out.println("Money on local after changing: " + localAccount.getAmount());
    }
}
