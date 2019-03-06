package atm;

import account.Account;
import account.ChequingAccount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

final class AccountStorageManager {
    private Map<Class, ArrayList<Object>> accountMapByType;
    private ChequingAccount primaryAccount;

    AccountStorageManager() {
        accountMapByType = new HashMap<>();
    }

    public ChequingAccount getPrimaryAccount() {
        return primaryAccount;
    }

    public void setPrimaryAccount(ChequingAccount account) {
        if (!getAllAccounts().contains(account))
            throw new IllegalArgumentException("Given account does not belong to this user.");

        primaryAccount = account;
    }

    public <T extends Account> void addAccount(T account) {
        Class klass = account.getClass();
        add(klass, account);

        Class parent = klass.getSuperclass();

        while (!parent.equals(Object.class)) {
            add(parent, account);

            for (Class c : parent.getInterfaces())
                add(c, account);

            parent = parent.getSuperclass();
        }

        Class[] interfaces = klass.getInterfaces();

        for (Class c : interfaces)
            add(c, account);

    }

    private void add(Class klass, Object account) {
        if (accountMapByType.containsKey(klass)) {
            accountMapByType.get(klass).add(account);
        } else {
            ArrayList<Object> list = new ArrayList<>();
            list.add(account);
            accountMapByType.put(klass, list);
        }
    }

    public <T> ArrayList<T> getAccountListOfType(Class<T> klass) {
        ArrayList<T> result = new ArrayList<>();

        if (!accountMapByType.containsKey(klass))
            return result;

        for (Object obj : accountMapByType.get(klass))
            result.add(klass.cast(obj));

        return result;
    }

    public ArrayList<Account> getAllAccounts() {
        ArrayList<Account> result = new ArrayList<>();

        for (Object obj : accountMapByType.get(Account.class))
            result.add((Account) obj);

        return result;
    }

}
