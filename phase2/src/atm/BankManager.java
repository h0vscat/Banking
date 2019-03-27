package atm;

import account.Account;
import account.BillingAccount;
import account.ChequingAccount;
import account.SavingsAccount;
import transaction.Transaction;
import ui.Console;

import java.io.Serializable;
import java.util.*;

/**
 * The manager class represent a manger, responsible for managing accounts and initialize time for the ATMs.
 *
 * @author zhaojuna
 * @version 1.0
 */
public class BankManager implements Serializable {
    private AtmTime commonTime;
    private List<AtmMachine> machineList;
    private AccountFactory accountFactory;
    private UserDatabase userDatabase;
    private List<BillingAccount> payeeList;
    private boolean hasLoggedin;
    private String username, password;
    private PasswordManager passwordManager;

    private boolean hasInitialized;

    public BankManager() {
        machineList = new ArrayList<>();
        userDatabase = new UserDatabase();
        accountFactory = new AccountFactory();
        hasInitialized = false;
        hasLoggedin = false;
        username = "admin";
        password = "CS207fun";
        passwordManager = new PasswordManager(12, 24, "[0-9]|[a-z]|[A-Z]");

        addMachine();
    }

    public void initialize(Date initialDate, Console activateConsole) {
        commonTime = new AtmTime(initialDate, activateConsole);

        try {
            payeeList = accountFactory.getPayeesFromFile(commonTime);
        } catch (IllegalFileFormatException e) {
            System.out.println(e.getMessage());
        }

        hasInitialized = true;
    }

    public List<BillingAccount> getPayeeList() {
        return payeeList;
    }

    /**
     * Get the Atm's time
     *
     * @return The time of this ATM.
     */
    public AtmTime getCommonTime() {
        return commonTime;
    }

    public boolean hasInitialized() {
        return hasInitialized;
    }

    public boolean isValidPassword(String password) {
        return passwordManager.isValidPassword(password);
    }

    /**
     * Takes the user's username and log the user in.
     *
     * @param username Username of the user.
     * @param password Password of the user.
     * @throws WrongPasswordException Throws this exception if the password is incorrect.
     * @throws UserNotExistException  Throws this exception if the username does not exist.
     */
    public void login(String username, String password) throws WrongPasswordException, UserNotExistException {
        if (username.equals(this.username)) {
            if (password.equals(this.password))
                hasLoggedin = true;
            else
                throw new WrongPasswordException(username);
        } else {
            throw new UserNotExistException(username);
        }

    }

    /**
     * Check if the manager has logged in ot not.
     *
     * @return Log in states of the manager.
     */

    public boolean hasLoggedin() {
        return hasLoggedin;
    }

    public void logout() {
        hasLoggedin = false;
    }

    public List<User> getAllUsers() {
        return userDatabase.getUserList();
    }

    /**
     * Private method that checks if the manager account is in a correct state.
     *
     * @param needLoginState True if manager need to log in.
     */
    private void checkState(boolean needLoginState) {
        if (!hasInitialized)
            throw new IllegalStateException("Manager not yet initialized!");

        if (needLoginState && !hasLoggedin)
            throw new IllegalStateException("This manager is not logged in yet!");
    }

    public List<AtmMachine> getMachineList() {
        return Collections.unmodifiableList(machineList);
    }

    public void restockMachine(TreeMap<Integer, Integer> stock) {
        try {
            machineList.get(0).increaseStock(stock);
        } catch (InvalidCashTypeException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Add a new ATM Machine with stock to the manager.
     *
     * @return The new ATM machine that has been created.
     */
    private AtmMachine addMachine() {
        TreeMap<Integer, Integer> initialStock = new TreeMap<>();
        initialStock.put(5, 100);
        initialStock.put(10, 100);
        initialStock.put(20, 100);
        initialStock.put(50, 100);

        AtmMachine machine = new AtmMachine(commonTime, initialStock, new StepCashDistributor());
        machineList.add(machine);
        return machine;
    }

    /**
     * Create new account to the correspond user.
     *
     * @param username    The user that account need to be added to.
     * @param accountType The type of the account.
     * @param isPrimary   if the account is the primary account of the user or not.
     * @param <T>         Any account type.
     * @return Return the account created.
     */
    public <T extends Account> boolean createAccount(String username, Class<T> accountType, boolean isPrimary) {
        checkState(true);

        return accountFactory.generateDefaultAccount(userDatabase.getUser(username), null, accountType, commonTime, isPrimary);
    }

    /**
     * Create joint account to the correspond user.
     *
     * @param user1name    The first user that account need to be added to.
     * @param user2name    The second user that account need to be added to.
     * @param accountType The type of the account.
     * @param isPrimary   if the account is the primary account of the user or not.
     * @param <T>         Any account type.
     * @return Return the account created.
     */
    public <T extends Account> boolean createJointAccount(
            String user1name, String user2name, Class<T> accountType, boolean isPrimary) {
        checkState(true);

        return accountFactory.generateDefaultAccount(
                userDatabase.getUser(user1name), userDatabase.getUser(user2name), accountType, commonTime, isPrimary);
    }


    /**
     * Create a new employee account.
     * @param Username Username of the employee.
     * @return The password of the account.
     * @throws UsernameAlreadyExistException When username already exist, this exception will be thrown.
     * @throws UsernameOutOfRangeException When username is too long, this exception is thrown.
     */
    public String createEmployee(String Username)throws UsernameAlreadyExistException, UsernameOutOfRangeException {
        checkState(true);

        if (username.length() < User.MIN_NAME_LENGTH || username.length() > User.MAX_NAME_LENGTH) {
            throw new UsernameOutOfRangeException();
        }
        String password = passwordManager.generateRandomPassword();

        Employee newEmployee = userDatabase.registerNewEmployee(Username, password, this.commonTime,this);
        accountFactory.generateDefaultAccount(newEmployee, null, ChequingAccount.class,commonTime,true);

        return password;
    }
    /**
     * Create a new user with given username and register it in the database
     *
     * @param username The username of the user
     * @return The default password for this user
     */
    public String createUser(String username) throws UsernameAlreadyExistException, UsernameOutOfRangeException {
        checkState(true);

        if (username.length() < User.MIN_NAME_LENGTH || username.length() > User.MAX_NAME_LENGTH)
            throw new UsernameOutOfRangeException();

        String password = passwordManager.generateRandomPassword();
        User newUser = userDatabase.registerNewUser(username, password);

        accountFactory.generateDefaultAccount(newUser, null, ChequingAccount.class, commonTime, true);

        return password;
    }

    /**
     * Create a new Child user with a chequing and a savings account.
     * @param username Username of the user
     * @param parent   The parent user.
     * @return  The password of the user.
     */
    public String creatChildUser(String username, User parent) throws UsernameAlreadyExistException, UsernameOutOfRangeException {
        checkState(true);

        if (username.length() < User.MIN_NAME_LENGTH || username.length() > User.MAX_NAME_LENGTH)
            throw new UsernameOutOfRangeException();

        String password = passwordManager.generateRandomPassword();
        User newUser = userDatabase.registerNewChildUser(username, password,parent);

        accountFactory.generateDefaultAccount(newUser,parent,ChequingAccount.class,commonTime,true);
        accountFactory.generateDefaultAccount(newUser,parent,SavingsAccount.class,commonTime,false);

        return password;
    }
    /**
     * Try to log user into the system, throws error if the user failed to log in.
     *
     * @param username User's username.
     * @param password User's password.
     * @return The user account correspond to the username.
     */
    public User validateUserLogin(String username, String password) {
        checkState(false);

        User user = null;

        try {
            user = userDatabase.loginUser(username, password);
        } catch (WrongPasswordException | UserNotExistException e) {
            System.out.println(e.getMessage());
        }

        return user;
    }

    /**
     * Cancel the last transaction made by the specific user account.
     *
     * @param targetAccount The account requires cancellation.
     * @return Return true if cancellation is successful, return false if the last transaction is not empty.
     */
    public boolean cancelLastTransaction(Account targetAccount) {
        checkState(true);

        Transaction transaction = targetAccount.getLastTransaction();

        if (transaction != null && transaction.isCancellable()) {
            transaction.cancel();
            return true;
        }

        return false;
    }

    /**
     * This method is responsible for cancelling multiple recent transactions.
     * @param targetAccount         User account that the cancellation wil be perform on.
     * @param numberOfTransactions  Numbers of transaction you want to cancel.
     * @return                      Whether the cancellation is performed or not.
     */
    public boolean cancelTransactions(Account targetAccount, int numberOfTransactions) {
        checkState(true);

        List<Transaction> fullTransaction = targetAccount.getFullTransaction();
        if (fullTransaction.size() == 0) {
            return true;
        } else if (fullTransaction.size() < numberOfTransactions) {
            numberOfTransactions = fullTransaction.size();
        }
        for (int index = fullTransaction.size() - 1; index >= fullTransaction.size() - 1 - numberOfTransactions; index--) {
            Transaction transaction = fullTransaction.get(index);
            if (!transaction.isCancellable()) {
                if (!transaction.isCancelled()) {
                    transaction.cancel();
                }
            }
        }
        return true;


    }

}
