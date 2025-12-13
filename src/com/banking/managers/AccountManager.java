package com.banking.managers;

import com.banking.models.*;
import com.banking.utilities.*;
import com.banking.BankingSystem;
import java.util.*;

public class AccountManager {
    private LinkedList<Customer> customers;
    private LinkedList<Account> accountList;
    private InputValidator validator;
    private BankingSystem bankingSystem;  // Reference for permission checks and audit logging
    private int accountCounter = 1;  // Auto-generate Account Numbers (ACC001, ACC002, etc.)

    public AccountManager(LinkedList<Customer> customers, LinkedList<Account> accountList, InputValidator validator) {
        this.customers = customers;
        this.accountList = accountList;
        this.validator = validator;
        this.bankingSystem = null;  // Set later via setBankingSystem()
    }


    public String generateNextAccountNumber() {
        // Find the highest existing account number
        int maxNum = 0;
        for (Account a : this.accountList) {
            String accNo = a.getAccountNo();
            if (accNo.startsWith("ACC") && accNo.length() == 6) {
                try {
                    int num = Integer.parseInt(accNo.substring(3));
                    if (num > maxNum) {
                        maxNum = num;
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid account numbers
                }
            }
        }

        // Next account number is maxNum + 1
        this.accountCounter = maxNum + 1;
        return "ACC" + String.format("%03d", this.accountCounter++);
    }


    public Account createAccount(String customerId, String accountType, String accountNo) {
        // Calls this.findCustomer() to search LinkedList
        Customer customer = this.findCustomer(customerId);
        if (customer == null) {
            UIFormatter.printError("Customer not found: " + customerId);
            return null;
        }

        // Calls this.validateAccountExists() to prevent duplicates
        if (this.validateAccountExists(accountNo)) {
            UIFormatter.printError("Account number already exists: " + accountNo);
            return null;
        }

        try {
            Account account = null;
            if (accountType.equalsIgnoreCase("SAVINGS")) {
                // Creates SavingsAccount (polymorphic - extends Account)
                account = new SavingsAccount(accountNo, customer, 0.03);  // 3% interest
            } else if (accountType.equalsIgnoreCase("CHECKING")) {
                // Creates CheckingAccount (polymorphic - extends Account)
                account = new CheckingAccount(accountNo, customer, 500.0);  // $500 overdraft limit
            } else {
                UIFormatter.printError("Invalid account type: " + accountType + " (Must be either SAVINGS or CHECKING)");
                return null;
            }

            // Note: No null check needed here - 'new' operator cannot return null
            // If validation fails, constructor throws IllegalArgumentException (caught below)

            // Add to system LinkedList
            this.accountList.add(account);
            // Calls Customer.addAccount() to establish bidirectional relationship
            customer.addAccount(account);
            // Calls Account.getDetails() - polymorphic (SavingsAccount vs CheckingAccount)
            UIFormatter.printSuccess("Account created: " + account.getDetails());
            return account;
        } catch (IllegalArgumentException e) {
            UIFormatter.printError("Error creating account: " + e.getMessage());
            return null;
        }
    }

    public Account createAndDisplayAccount(Customer customer, String accountType) {
        if (customer == null) {
            UIFormatter.printError("Customer is null");
            return null;
        }

        // Auto-generate account number
        String accountNo = this.generateNextAccountNumber();

        // Create the account using core CRUD method
        Account account = this.createAccount(customer.getCustomerId(), accountType, accountNo);
        if (account != null) {
            UIFormatter.printSuccess("Account successfully created and linked to customer!",
                    "Account Number: " + accountNo,
                    "Type: " + accountType,
                    "Balance: $0.00");

            // Log the action
            if (this.bankingSystem != null && this.bankingSystem.getCurrentUser() != null) {
                this.bankingSystem.logAction("CREATE_ACCOUNT",
                        "Account: " + accountNo + " Type: " + accountType + " Customer: " + customer.getCustomerId());
            }
        }
        return account;
    }

    public boolean deleteAccount(String accountNo) {
        Account account = this.findAccount(accountNo);
        if (account == null) {
            UIFormatter.printError("Account not found: " + accountNo);
            return false;
        }

        this.accountList.remove(account);
        Customer owner = account.getOwner();
        if (owner != null) {
            owner.removeAccount(accountNo);
        } else {
            System.out.println("⚠ Warning: Account had no owner, only removed from system");
        }
        UIFormatter.printSuccess("Account deleted: " + accountNo);
        return true;
    }

    public Account findAccount(String accountNo) {
        return AccountUtils.findAccount(this.accountList, accountNo);
    }

    public boolean validateAccountExists(String accountNo) {
        return this.findAccount(accountNo) != null;
    }


    private void insertionSortByName(LinkedList<Account> accountList) {
        // For each unsorted element (starting at index 1, first element already "sorted")
        for (int i = 1; i < accountList.size(); i++) {
            // Get current account to insert into sorted portion
            Account currentAccount = accountList.get(i);
            String currentName = (currentAccount.getOwner() != null)
                    ? currentAccount.getOwner().getName() : "";
            // Find the correct position to insert currentAccount in sorted portion
            int j = i - 1;

            while (j >= 0) {
                Account compareAccount = accountList.get(j);
                String compareName = (compareAccount.getOwner() != null)
                        ? compareAccount.getOwner().getName() : "";

                // If current name comes before compare name alphabetically, shift right
                if (currentName.compareToIgnoreCase(compareName) < 0) {
                    accountList.set(j + 1, compareAccount);  // Shift element right
                    j--;  // Continue searching left
                } else {
                    break;  // Found correct position - stop searching
                }
            }

            // Insert current account at its correct sorted position
            accountList.set(j + 1, currentAccount);
        }
    }

    public void sortAccountsByName() {
        this.insertionSortByName(this.accountList);
        UIFormatter.printSuccess("Accounts sorted by customer name");
    }

    private void insertionSortByBalance(LinkedList<Account> accountList) {
        // Iterate through each account starting from index 1
        for (int i = 1; i < accountList.size(); i++) {
            Account currentAccount = accountList.get(i);
            double currentBalance = currentAccount.getBalance();

            // Find the correct position to insert currentAccount (descending order)
            int j = i - 1;
            while (j >= 0) {
                Account compareAccount = accountList.get(j);
                double compareBalance = compareAccount.getBalance();

                // If current balance is greater than compare balance, shift right (descending)
                if (currentBalance > compareBalance) {
                    accountList.set(j + 1, compareAccount);
                    j--;
                } else {
                    break;  // Found correct position
                }
            }

            // Insert current account at correct position
            accountList.set(j + 1, currentAccount);
        }
    }

    public void sortAccountsByBalance() {
        this.insertionSortByBalance(this.accountList);
        UIFormatter.printSuccess("Accounts sorted by balance (descending)");
    }

    public void handleSortByName() {
        UIFormatter.printSectionHeader("SORT ACCOUNTS BY NAME");

        if (this.accountList.isEmpty()) {
            UIFormatter.printInfo("No accounts found. Use option 5 (Create Account) to add one.");
            return;
        }

        this.sortAccountsByName();

        System.out.println("\n--- AFTER SORTING (By Customer Name) ---");
        System.out.println();
        UIFormatter.printTableHeader("Account No", "Type", "Owner", "Balance");
        for (Account account : this.accountList) {
            String accountNo = account.getAccountNo();
            String type = (account instanceof SavingsAccount) ? "Savings" : "Checking";
            String owner = (account.getOwner() != null) ? account.getOwner().getName() : "N/A";
            String balance = "$" + String.format("%.2f", account.getBalance());
            UIFormatter.printTableRow(accountNo, type, owner, balance);
        }
        UIFormatter.printTableFooter();
        System.out.println();

        InputValidator.safeLogAction(bankingSystem, "SORT_ACCOUNTS_BY_NAME",
            "Sorted " + this.accountList.size() + " accounts by customer name");
    }

    public void handleSortByBalance() {
        UIFormatter.printSectionHeader("SORT ACCOUNTS BY BALANCE");

        if (this.accountList.isEmpty()) {
            UIFormatter.printInfo("No accounts found. Use option 5 (Create Account) to add one.");
            return;
        }

        this.sortAccountsByBalance();

        System.out.println("\n--- AFTER SORTING (By Balance - Highest First) ---");
        System.out.println();
        UIFormatter.printTableHeader("Account No", "Type", "Owner", "Balance");
        for (Account account : this.accountList) {
            String accountNo = account.getAccountNo();
            String type = (account instanceof SavingsAccount) ? "Savings" : "Checking";
            String owner = (account.getOwner() != null) ? account.getOwner().getName() : "N/A";
            String balance = "$" + String.format("%.2f", account.getBalance());
            UIFormatter.printTableRow(accountNo, type, owner, balance);
        }
        UIFormatter.printTableFooter();
        System.out.println();

        InputValidator.safeLogAction(bankingSystem, "SORT_ACCOUNTS_BY_BALANCE",
            "Sorted " + this.accountList.size() + " accounts by balance (descending)");
    }


    public void handleApplyInterest() {
        UIFormatter.printSectionHeader("APPLY INTEREST TO SAVINGS ACCOUNTS");

        // Collect savings accounts and calculate interest
        LinkedList<String[]> interestResults = new LinkedList<>();
        for (Account acc : this.accountList) {
            if (acc instanceof SavingsAccount) {
                SavingsAccount savings = (SavingsAccount) acc;
                double oldBalance = savings.getBalance();
                savings.applyInterest();
                double newBalance = savings.getBalance();
                double interestEarned = newBalance - oldBalance;

                interestResults.add(new String[]{
                    savings.getAccountNo(),
                    "$" + String.format("%.2f", oldBalance),
                    "$" + String.format("%.2f", newBalance),
                    "$" + String.format("%.2f", interestEarned)
                });
            }
        }

        if (interestResults.isEmpty()) {
            UIFormatter.printInfo("No savings accounts found. Create accounts using option 5 and select SAVINGS type.");
            return;
        }

        System.out.println("\nInterest Applied: " + interestResults.size() + " savings account(s)");
        System.out.println();

        // Professional table format
        UIFormatter.printTableHeader("Account No", "Old Balance", "New Balance", "Interest Earned");
        for (String[] result : interestResults) {
            UIFormatter.printTableRow(result[0], result[1], result[2], result[3]);
        }
        UIFormatter.printTableFooter();

        UIFormatter.printSuccessEnhanced(
            "Interest applied successfully!",
            "Accounts Updated: " + interestResults.size(),
            "Status: Completed"
        );

        InputValidator.safeLogAction(bankingSystem, "APPLY_INTEREST",
            "Interest applied to " + interestResults.size() + " savings account(s)");
    }

    public boolean updateOverdraftLimit(String accountNo, double newLimit) {
        Account account = this.findAccount(accountNo);
        if (account == null) {
            UIFormatter.printError("Account not found");
            return false;
        }

        if (!(account instanceof CheckingAccount)) {
            UIFormatter.printError("This is not a checking account. Only checking accounts have overdraft limits.");
            return false;
        }

        CheckingAccount checking = (CheckingAccount) account;
        try {
            checking.setOverdraftLimit(newLimit);
            UIFormatter.printSuccess("Overdraft limit updated to: $" + checking.getOverdraftLimit());
            return true;
        } catch (IllegalArgumentException e) {
            UIFormatter.printError("Error: " + e.getMessage());
            return false;
        }
    }

    public void handleUpdateOverdraftLimit() {
        UIFormatter.printSectionHeader("UPDATE OVERDRAFT LIMIT");

        // Unified retry loop: handles BOTH "not found" AND "wrong type"
        Account account = null;
        while (account == null) {
            String accNo = this.validator.getValidatedInput("Account Number (Checking account only):",
                    com.banking.utilities.ValidationPatterns.ACCOUNT_NO_PATTERN,
                    "(format: " + com.banking.utilities.ValidationPatterns.ACCOUNT_NO_FORMAT + " e.g., ACC001)");
            if (accNo == null) return;  // User cancelled

            Account candidate = this.findAccount(accNo);

            // ERROR 1: Account not found
            if (candidate == null) {
                UIFormatter.printError("Account not found. Please try again.");
                System.out.println("   Enter a valid account number or type 'back' to cancel.\n");
                continue;  // Retry
            }

            // ERROR 2: Wrong account type (THE FIX!)
            if (!(candidate instanceof CheckingAccount)) {
                UIFormatter.printError("This is not a checking account. Only checking accounts have overdraft limits.");
                System.out.println("   Please select a CHECKING account or type 'back' to cancel.\n");
                continue;  // Retry instead of returning
            }

            // SUCCESS: Valid checking account found
            account = candidate;
        }

        String accNo = account.getAccountNo();

        CheckingAccount checking = (CheckingAccount) account;
        System.out.println("Current overdraft limit: $" + checking.getOverdraftLimit());

        Double newLimit = this.validator.getValidatedAmountWithLabel("New overdraft limit:");
        if (newLimit == null) return;

        boolean success = this.updateOverdraftLimit(accNo, newLimit);
        if (success) {
            UIFormatter.printSuccess("Overdraft limit updated successfully!");
            InputValidator.safeLogAction(bankingSystem, "UPDATE_OVERDRAFT_LIMIT", "Account: " + accNo + " New Limit: $" + newLimit);
        } else {
            UIFormatter.printError("Failed to update overdraft limit. Please try again.");
        }
    }


    public void handleCreateAccount() {
        UIFormatter.printSectionHeader("CREATE ACCOUNT");

        while (true) {  // OUTER RETRY LOOP
            // Step 1: Get the customer
            Customer customer = this.validator.getValidatedCustomer(
                    "✗ Customer not found. Please create customer first (Option 1).");
            if (customer == null) return;  // User cancelled - exit immediately

            // Step 2: Get account type from user
            String type = this.validator.getValidatedAccountType("Account type (SAVINGS or CHECKING):");
            if (type == null) return;  // User cancelled - exit immediately

            // Step 3: Create account using unified helper (auto-generates number, displays result, logs action)
            Account account = this.createAndDisplayAccount(customer, type);
            if (account != null) {
                break;  // Success - exit loop
            } else {
                UIFormatter.printError("Account creation failed.");

                if (!this.validator.confirmAction("Try again?")) {
                    return;  // User chose no - exit
                }
                // User chose yes - retry from top
            }
        }
    }

    public void handleViewAllAccounts() {
        UIFormatter.printSectionHeader("VIEW ALL ACCOUNTS");

        if (this.accountList.isEmpty()) {
            UIFormatter.printInfo("No accounts found. Use option 5 (Create Account) to add one.");
            return;
        }

        System.out.println("\nTotal Accounts: " + this.accountList.size());
        System.out.println();

        // Professional table format
        UIFormatter.printTableHeader("Account No", "Type", "Owner", "Balance");

        for (Account account : this.accountList) {
            String accountNo = account.getAccountNo();
            String type = (account instanceof SavingsAccount) ? "Savings" : "Checking";
            String owner = (account.getOwner() != null) ? account.getOwner().getName() : "N/A";
            String balance = "$" + String.format("%.2f", account.getBalance());

            UIFormatter.printTableRow(accountNo, type, owner, balance);
        }

        UIFormatter.printTableFooter();
        System.out.println();
    }

    public void handleViewAccountDetails() {
        UIFormatter.printSectionHeader("VIEW ACCOUNT DETAILS");

        // Use access-controlled input for customers
        Account account = this.validator.getValidatedAccountWithAccessControl(this.bankingSystem.getCurrentUser());
        if (account == null) return;

        // Professional box format for account details
        System.out.println();
        UIFormatter.printTopBorder();
        UIFormatter.printCenteredLine("ACCOUNT INFORMATION");
        UIFormatter.printMiddleBorder();

        Customer owner = account.getOwner();
        UIFormatter.printLeftAlignedLine("  Account Number:     " + account.getAccountNo(), 0);
        UIFormatter.printLeftAlignedLine("  Account Type:       " +
            (account instanceof SavingsAccount ? "SAVINGS" : "CHECKING"), 0);
        UIFormatter.printLeftAlignedLine("  Owner:              " +
            (owner != null ? owner.getName() : "NO OWNER"), 0);
        UIFormatter.printLeftAlignedLine("  Owner ID:           " +
            (owner != null ? owner.getCustomerId() : "N/A"), 0);
        UIFormatter.printLeftAlignedLine("  Current Balance:    $" +
            String.format("%.2f", account.getBalance()), 0);

        // Type-specific details
        if (account instanceof SavingsAccount) {
            SavingsAccount savings = (SavingsAccount) account;
            UIFormatter.printLeftAlignedLine("  Interest Rate:      " +
                String.format("%.2f%%", savings.getInterestRate() * 100), 0);
        } else if (account instanceof CheckingAccount) {
            CheckingAccount checking = (CheckingAccount) account;
            UIFormatter.printLeftAlignedLine("  Overdraft Limit:    $" +
                String.format("%.2f", checking.getOverdraftLimit()), 0);
            UIFormatter.printLeftAlignedLine("  Available Credit:   $" +
                String.format("%.2f", checking.getBalance() + checking.getOverdraftLimit()), 0);
        }

        LinkedList<Transaction> txHistory = account.getTransactionHistory();
        UIFormatter.printLeftAlignedLine("  Total Transactions: " +
            (txHistory != null ? txHistory.size() : 0), 0);

        UIFormatter.printBottomBorder();
        System.out.println();
    }

    public void handleDeleteAccount() {
        UIFormatter.printSectionHeader("DELETE ACCOUNT");

        Account account = null;
        while (account == null) {  // RETRY LOOP for account lookup
            String accNo = this.validator.getValidatedInputWithFeedback(
                    "Account Number:",
                    com.banking.utilities.ValidationPatterns.ACCOUNT_NO_PATTERN,
                    "(format: " + com.banking.utilities.ValidationPatterns.ACCOUNT_NO_FORMAT + " e.g., ACC001)");
            if (accNo == null) return;  // User cancelled - exit immediately

            account = this.findAccount(accNo);
            if (account == null) {
                UIFormatter.printErrorEnhanced(
                        "Account does not exist",
                        "Use 'View All Accounts' to see valid account numbers.");

                if (!this.validator.confirmAction("Try again?")) {
                    return;  // User chose no - exit
                }
                // User chose yes - retry (loop continues)
            }
        }

        // Show account details before deletion
        System.out.println("\nAccount to be deleted:");
        UIFormatter.printDataRow("  Account Number:", account.getAccountNo());
        UIFormatter.printDataRow("  Type:", account instanceof SavingsAccount ? "SAVINGS" : "CHECKING");
        UIFormatter.printDataRow("  Owner:", account.getOwner() != null ? account.getOwner().getName() : "N/A");
        UIFormatter.printDataRow("  Balance:", "$" + String.format("%.2f", account.getBalance()));
        System.out.println();

        // Enhanced confirmation
        if (!this.validator.confirmActionEnhanced(
                "Are you sure you want to delete account " + account.getAccountNo() + "?",
                "This action cannot be undone. All transaction history will be lost.")) {
            return;
        }

        UIFormatter.printLoading("Deleting account");
        boolean success = this.deleteAccount(account.getAccountNo());

        if (success) {
            UIFormatter.printSuccessEnhanced(
                    "Account deleted successfully!",
                    "Account Number: " + account.getAccountNo(),
                    "Owner: " + (account.getOwner() != null ? account.getOwner().getName() : "N/A"),
                    "Status: Deleted");
            InputValidator.safeLogAction(bankingSystem, "DELETE_ACCOUNT", "Account: " + account.getAccountNo());
        } else {
            UIFormatter.printErrorEnhanced(
                    "Failed to delete account",
                    "The account may have dependencies. Please try again.");
        }
    }

    // Note: canAccessAccount() check is delegated to BankingSystem.canAccessAccount()
    // This ensures a single source of truth for authorization logic across the entire system.

    private Customer findCustomer(String customerId) {
        for (Customer c : this.customers) {
            if (c.getCustomerId().equals(customerId)) {
                return c;
            }
        }
        return null;
    }



    public void setBankingSystem(BankingSystem bankingSystem) {
        this.bankingSystem = bankingSystem;
    }

}
