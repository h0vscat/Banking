package account;

import atm.User;
import jdk.internal.org.objectweb.asm.tree.TryCatchBlockNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import transaction.DepositTransaction;
import transaction.Transaction;
import transaction.WithdrawTransaction;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class SavingAccountTest {

    private Date time;
    private User owner;
    private Transaction register;
    private SavingsAccount savingsAccount;
    private Object WithdrawException;


    @Before
    public void setup() {
        time = new Date();
        owner = Mockito.mock(User.class);
    }

    @Test
    public void testGrow(){
        savingsAccount = new SavingsAccount(time, owner, 0.001, "30", 100);
        savingsAccount.grow();
        assertEquals(100 * 1.001, savingsAccount.getBalance(), 0.0);
    }

    @Test
    public void testPositive() throws Throwable {
        register = Mockito.mock(WithdrawTransaction.class);
        savingsAccount = new SavingsAccount(time, owner);
        System.out.println(savingsAccount.getBalance());
        try {
            savingsAccount.withdraw(50, register);
        } catch (InsufficientFundException i) {
            i.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Incorrect exception thrown!");
        }
    }
}