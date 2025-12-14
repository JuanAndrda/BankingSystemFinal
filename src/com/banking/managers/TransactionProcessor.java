package com.banking.managers;

import com.banking.models.*;
import com.banking.utilities.*;
import com.banking.BankingSystem;
import java.util.*;

// Transaction processor - demonstrates COMPOSITION (HAS-A relationship)
public class TransactionProcessor {
    private LinkedList<Account> accountList;
    private int txCounter;
    private InputValidator validator;
    private BankingSystem bankingSystem;

    public TransactionProcessor(LinkedList<Account> accountList, InputValidator validator) {
        this.accountList = accountList;
        this.txCounter = 1;
        this.validator = validator;
        this.bankingSystem = null;
    }

    public boolean deposit(String accountNo, double amount) {
        Account account = AccountUtils.findAccount(this.accountList, accountNo);
        if (account == null) {
            UIFormatter.printError("Account not found");
            return false;
        }

        try {
            Transaction tx = new Transaction("TX" + String.format("%03d", this.txCounter++), TransactionType.DEPOSIT, amount);
            tx.setToAccountNo(accountNo);

            account.deposit(amount);
            tx.setStatus("COMPLETED");
            account.addTransaction(tx);
            UIFormatter.printSuccess("Deposit processed: " + tx.getTxId());
            return true;
        } catch (IllegalArgumentException e) {
            UIFormatter.printError("Error processing deposit: " + e.getMessage());
            return false;
        }
    }

    public boolean withdraw(String accountNo, double amount) {
        Account account = AccountUtils.findAccount(this.accountList, accountNo);
        if (account == null) {
            UIFormatter.printError("Account not found");
            return false;
        }

        try {
            Transaction tx = new Transaction("TX" + String.format("%03d", this.txCounter++), TransactionType.WITHDRAW, amount);
            tx.setFromAccountNo(accountNo);

            if (account.withdraw(amount)) {
                tx.setStatus("COMPLETED");
                account.addTransaction(tx);
                UIFormatter.printSuccess("Withdrawal processed: " + tx.getTxId());
                return true;
            } else {
                tx.setStatus("FAILED");
                account.addTransaction(tx);
                UIFormatter.printError("Insufficient funds or withdrawal failed");
                return false;
            }
        } catch (IllegalArgumentException e) {
            UIFormatter.printError("Error processing withdrawal: " + e.getMessage());
            return false;
        }
    }

    public boolean transfer(String fromAccountNo, String toAccountNo, double amount) {
        Account fromAccount = AccountUtils.findAccount(this.accountList, fromAccountNo);
        Account toAccount = AccountUtils.findAccount(this.accountList, toAccountNo);

        if (fromAccount == null || toAccount == null) {
            UIFormatter.printError("One or both accounts not found");
            return false;
        }

        try {
            Transaction tx = new Transaction("TX" + String.format("%03d", this.txCounter++), TransactionType.TRANSFER, amount);
            tx.setFromAccountNo(fromAccountNo);
            tx.setToAccountNo(toAccountNo);

            if (fromAccount.withdraw(amount)) {
                toAccount.deposit(amount);
                tx.setStatus("COMPLETED");
                fromAccount.addTransaction(tx);
                toAccount.addTransaction(tx);
                UIFormatter.printSuccess("Transfer processed: " + tx.getTxId());
                return true;
            } else {
                tx.setStatus("FAILED");
                fromAccount.addTransaction(tx);
                UIFormatter.printError("Transfer failed: Insufficient funds");
                return false;
            }
        } catch (IllegalArgumentException e) {
            UIFormatter.printError("Error processing transfer: " + e.getMessage());
            return false;
        }
    }


    public void handleDeposit() {
        UIFormatter.printSectionHeader("DEPOSIT MONEY");

        while (true) {  // OUTER RETRY LOOP
            Account account = this.validator.getValidatedAccount("Account to deposit to:", "✗ Account not found. Cannot deposit to non-existent account.");
            if (account == null) return;  // User cancelled - exit immediately

            if (!this.bankingSystem.canAccessAccount(account.getAccountNo())) {
                UIFormatter.printErrorEnhanced("Access denied. You can only perform transactions on your own accounts.", "Please select one of your accounts own accounts");
                // Log access denial for audit trail
                this.bankingSystem.logAction("ACCESS_DENIED", "Attempted to deposit to account: " + account.getAccountNo());

                if (!this.validator.confirmAction("Try again with a different account?")) {
                    return;  // User chose no - exit
                }
                continue;  // User chose yes - retry from top
            }

            Double depAmt = this.validator.getValidatedAmountWithLabel("Amount to deposit:");
            if (depAmt == null) return;  // User cancelled - exit immediately

            boolean success = this.deposit(account.getAccountNo(), depAmt);
            if (success) {
                UIFormatter.printSuccessEnhanced("Deposit successful!", "Amount: $" + String.format("%.2f", depAmt), "Account: " + account.getAccountNo(), "New Balance: $" + String.format("%.2f", account.getBalance()));
                InputValidator.safeLogAction(bankingSystem, "DEPOSIT", "Amount: $" + depAmt + " to account: " + account.getAccountNo());
                break;  // Success - exit loop
            } else {
                UIFormatter.printError("Deposit failed.");

                if (!this.validator.confirmAction("Try again?")) {
                    return;  // User chose no - exit
                }
                // User chose yes - retry from top
            }
        }
    }

    public void handleWithdraw() {
        UIFormatter.printSectionHeader("WITHDRAW MONEY");

        while (true) {  // OUTER RETRY LOOP
            Account account = this.validator.getValidatedAccount("Account to withdraw from:", "✗ Account not found. Cannot withdraw from non-existent account.");
            if (account == null) return;  // User cancelled - exit immediately

            if (!this.bankingSystem.canAccessAccount(account.getAccountNo())) {
                UIFormatter.printErrorEnhanced("Access denied. You can only perform transactions on your own accounts.", "Please select one of your own account."
                );
                // Log access denial for audit trail
                this.bankingSystem.logAction("ACCESS_DENIED", "Attempted to withdraw from account: " + account.getAccountNo());

                if (!this.validator.confirmAction("Try again with a different account?")) {
                    return;  // User chose no - exit
                }
                continue;  // User chose yes - retry from top
            }

            Double witAmt = this.validator.getValidatedAmountWithLabel("Amount to withdraw:");
            if (witAmt == null) return;  // User cancelled - exit immediately

            boolean success = this.withdraw(account.getAccountNo(), witAmt);
            if (success) {
                UIFormatter.printSuccessEnhanced("Withdrawal successful!", "Amount: $" + String.format("%.2f", witAmt), "Account: " + account.getAccountNo(), "New Balance: $" + String.format("%.2f", account.getBalance()));
                InputValidator.safeLogAction(bankingSystem, "WITHDRAW", "Amount: $" + witAmt + " from account: " + account.getAccountNo());
                break;  // Success - exit loop
            } else {
                UIFormatter.printError("Withdrawal failed.");

                if (!this.validator.confirmAction("Try again?")) {
                    return;  // User chose no - exit
                }
                // User chose yes - retry from top
            }
        }
    }

    public void handleTransfer() {
        UIFormatter.printSectionHeader("TRANSFER MONEY");

        while (true) {  // OUTER RETRY LOOP
            Account fromAccount = this.validator.getValidatedAccount("Source account (to transfer FROM):", "✗ Source account not found. Cannot transfer from non-existent account.");
            if (fromAccount == null) return;  // User cancelled - exit immediately

            if (!this.bankingSystem.canAccessAccount(fromAccount.getAccountNo())) {
                UIFormatter.printErrorEnhanced("Access denied. You can only transfer FROM your own accounts.", "Please select one of your own.");
                // Log access denial for audit trail
                this.bankingSystem.logAction("ACCESS_DENIED",
                    "Attempted to transfer from account: " + fromAccount.getAccountNo());

                if (!this.validator.confirmAction("Try again with different accounts?")) {
                    return;  // User chose no - exit
                }
                continue;  // User chose yes - retry from top
            }

            Account toAccount = this.validator.getValidatedAccount("Destination account (to transfer TO):", "✗ Destination account not found. Cannot transfer to non-existent account.");
            if (toAccount == null) return;  // User cancelled - exit immediately

            if (fromAccount.getAccountNo().equals(toAccount.getAccountNo())) {
                UIFormatter.printError("Cannot transfer to the same account");

                if (!this.validator.confirmAction("Try again with different accounts?")) {
                    return;  // User chose no - exit
                }
                continue;  // User chose yes - retry from top
            }

            Double amt = this.validator.getValidatedAmountWithLabel("Amount to transfer:");
            if (amt == null) return;  // User cancelled - exit immediately

            boolean success = this.transfer(fromAccount.getAccountNo(), toAccount.getAccountNo(), amt);
            if (success) {
                UIFormatter.printSuccessEnhanced("Transfer successful!", "Amount: $" + String.format("%.2f", amt), "From: " + fromAccount.getAccountNo(), "To: " + toAccount.getAccountNo(), "New Balance (From): $" + String.format("%.2f", fromAccount.getBalance()));
                InputValidator.safeLogAction(bankingSystem, "TRANSFER", "Amount: $" + amt + " from " + fromAccount.getAccountNo() + " to " + toAccount.getAccountNo());
                break;  // Success - exit loop
            } else {
                UIFormatter.printError("Transfer failed.");

                if (!this.validator.confirmAction("Try again?")) {
                    return;  // User chose no - exit
                }
                // User chose yes - retry from top
            }
        }
    }

    public void handleViewTransactionHistory() {
        UIFormatter.printSectionHeader("VIEW TRANSACTION HISTORY");

        // FIX: Use access-controlled validator instead of regular getValidatedAccount()
        Account account = this.validator.getValidatedAccountWithAccessControl(this.bankingSystem.getCurrentUser()
        );
        if (account == null) return;

        Stack<Transaction> txStack = account.getTransactionHistory();

        if (txStack.isEmpty()) {
            UIFormatter.printInfo("No transactions yet. This is normal for new accounts.");
            return;
        }

        System.out.println("\n=== TRANSACTION HISTORY (LIFO - Most Recent First) ===");
        System.out.println("Account: " + account.getAccountNo());
        System.out.println("Total Transactions: " + txStack.size());
        System.out.println();

        // Professional table format - preserves LIFO Stack order
        UIFormatter.printTableHeader("TX ID", "Type", "Amount", "Status");

        Stack<Transaction> tempStack = (Stack<Transaction>) txStack.clone();

        while (!tempStack.isEmpty()) {
            Transaction tx = tempStack.pop();
            String txId = tx.getTxId();
            String type = tx.getType().toString();
            String amount = "$" + String.format("%.2f", tx.getAmount());
            String status = tx.getStatus();

            UIFormatter.printTableRow(txId, type, amount, status);
        }

        UIFormatter.printTableFooter();
        System.out.println();
    }

    public void setBankingSystem(BankingSystem bankingSystem) {
        this.bankingSystem = bankingSystem;
    }

}
