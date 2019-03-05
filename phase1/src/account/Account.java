package account;

import atm.User;
import transaction.Transaction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class Account implements Serializable {
    private final Date timeCreated;
    private final String id;
    private final User owner;
    private static int prev_id = 0;
    double balance;
    private List<Transaction> transactions;

    Account(Date time, User owner) {
        id = String.format("ACC%04d", prev_id);
        prev_id++;
        timeCreated = time;
        balance = 0.0;
        transactions = new ArrayList<>();
        this.owner = owner;
    }

    Account(Date time, User owner, double initialBalance) {
        this(time, owner);
        balance = initialBalance;
    }

    public User getOwner() {
        return owner;
    }

    public String getId() {
        return id;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    void registerTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public Transaction getLastTransaction() {

        for (int index = transactions.size() - 1; index >= 0; index--) {
            Transaction transaction = transactions.get(index);

            if (!transaction.isCancelled())
                return transaction;
        }

        return null;
    }

    public abstract double getNetBalance();

    public String toString(){
        return String.format("Owner: %s, ID: %s, Date Created: %s, Balance: %s", owner, getId(), timeCreated, balance);
    }
}
