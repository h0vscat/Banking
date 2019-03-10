package transaction;

import account.Account;
import account.BillingAccount;
import account.WithdrawException;
import account.Withdrawable;
import atm.ExternalFiles;
import atm.FileHandler;
import atm.User;

public class PayBillTransaction extends IntraUserTransaction {
    private final double payAmount;
    private final BillingAccount payee;
    private final Withdrawable payer;
    private final ExternalFiles file;

    public PayBillTransaction(User from, Withdrawable payer, BillingAccount payee, double amount) {
        super(from);
        file = ExternalFiles.BILLING_FILE;

        if (amount < 0)
            throw new IllegalArgumentException("Not allowed to pay negative amount: " + amount);

        payAmount = amount;
        this.payee = payee;
        this.payer = payer;
    }

    @Override
    protected boolean doPerform() {
        try {
            payer.withdraw(payAmount, this);
        } catch (WithdrawException e) {
            System.out.println(e.getMessage());
            return false;
        }

        payee.receivePay(payAmount, this);

        String msg = String.format("%s received $%.2f payment from %s",
                payee.getId(), payAmount, ((Account) payer).getId());

        (new FileHandler()).saveTo(file, msg);

        return true;
    }

    @Override
    protected boolean doCancel() {
        throw new IllegalStateException("Billing action is not cancellable!");
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
