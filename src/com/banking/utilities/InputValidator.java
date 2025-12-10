package com.banking.utilities;

import com.banking.models.*;
import com.banking.auth.User;
import com.banking.auth.UserAccount;
import com.banking.auth.UserRole;
import java.util.*;

// Input validation utility class
public class InputValidator {
    private LinkedList<Customer> customers;
    private LinkedList<Account> accountList;
    private Scanner sc;

    public InputValidator(LinkedList<Customer> customers, LinkedList<Account> accountList, Scanner sc) {
        this.customers = customers;
        this.accountList = accountList;
        this.sc = sc;
    }

    public Scanner getScanner() {
        return this.sc;
    }

    // Find customer by ID
    private Customer findCustomer(String customerId) {
        for (Customer c : this.customers) {
            if (c.getCustomerId().equals(customerId)) {
                return c;
            }
        }
        return null;
    }

    // Check if profile ID exists
    public boolean profileIdExists(String profileId) {
        for (Customer c : this.customers) {
            if (c.getProfile() != null && c.getProfile().getProfileId().equals(profileId)) {
                return true;
            }
        }
        return false;
    }

    // Get user input with cancellation support
    public String getCancellableInput(String prompt) {
        System.out.print(prompt + " (or type 'back' to cancel): ");
        String input = sc.nextLine().trim();
        if (input.equalsIgnoreCase("back")) {
            System.out.println("✗ Operation cancelled. Returning to menu...\n");
            return null;
        }
        return input;
    }

    // Get validated input matching pattern
    public String getValidatedInput(String prompt, String pattern, String formatHint) {
        while (true) {
            String input = this.getCancellableInput(prompt + " " + formatHint);
            if (input == null) return null;  // User cancelled

            if (input.matches(pattern)) {
                return input;
            } else {
                System.out.println("✗ Invalid format! " + formatHint);
                System.out.println("   Please try again...\n");
            }
        }
    }

    // Get validated non-empty string
    public String getValidatedString(String prompt) {
        while (true) {
            String input = this.getCancellableInput(prompt);
            if (input == null) return null;

            if (!input.trim().isEmpty()) {
                return input.trim();
            } else {
                System.out.println("✗ This field cannot be empty!");
                System.out.println("   Please try again...\n");
            }
        }
    }

    // Get validated account type (SAVINGS or CHECKING)
    public String getValidatedAccountType(String prompt) {
        while (true) {
            String type = this.getCancellableInput(prompt);
            if (type == null) return null;

            if (type.equalsIgnoreCase("SAVINGS") || type.equalsIgnoreCase("CHECKING")) {
                return type.toUpperCase();
            } else {
                System.out.println("✗ Invalid account type! Must be either SAVINGS or CHECKING");
                System.out.println("   Please try again...\n");
            }
        }
    }

    // Get validated phone number (min 10 digits)
    public String getValidatedPhoneNumber(String prompt) {
        while (true) {
            String phone = this.getCancellableInput(prompt);
            if (phone == null) return null;  // User cancelled

            if (ValidationPatterns.isValidPhoneNumber(phone)) {
                return phone;  // Valid format
            } else {
                System.out.println("✗ " + ValidationPatterns.PHONE_ERROR);
                System.out.println("   Please try again...\n");
            }
        }
    }

    // Get validated email address
    public String getValidatedEmail(String prompt) {
        while (true) {
            String email = this.getCancellableInput(prompt);
            if (email == null) return null;  // User cancelled

            if (ValidationPatterns.matchesPattern(email, ValidationPatterns.EMAIL_PATTERN)) {
                return email;  // Valid format
            } else {
                System.out.println("✗ " + ValidationPatterns.EMAIL_ERROR);
                System.out.println("   Please try again...\n");
            }
        }
    }

    // Get validated customer (overloaded)
    public Customer getValidatedCustomer() {
        return this.getValidatedCustomer("✗ Customer not found");
    }

    // Get validated customer with custom error message (overloaded)
    public Customer getValidatedCustomer(String errorMessage) {
        String custId = this.getValidatedInput("Customer ID",
                ValidationPatterns.CUSTOMER_ID_PATTERN,
                "(format: " + ValidationPatterns.CUSTOMER_ID_FORMAT + " e.g., C001)");
        if (custId == null) return null;  // User cancelled

        Customer customer = this.findCustomer(custId);
        if (customer == null) {
            System.out.println(errorMessage);
        }
        return customer;
    }

    // Get validated account (overloaded)
    public Account getValidatedAccount() {
        return this.getValidatedAccount("✗ Account not found");
    }

    // Get validated account with custom error message (overloaded)
    // Includes retry loop for both format validation and account existence
    public Account getValidatedAccount(String errorMessage) {
        while (true) {
            String accNo = this.getValidatedInput("Account Number",
                    ValidationPatterns.ACCOUNT_NO_PATTERN,
                    "(format: " + ValidationPatterns.ACCOUNT_NO_FORMAT + " e.g., ACC001)");
            if (accNo == null) return null;  // User cancelled

            Account account = AccountUtils.findAccount(this.accountList, accNo);
            if (account == null) {
                System.out.println(errorMessage);
                System.out.println("   Please try again or type 'back' to cancel.\n");
                continue;  // RETRY
            }
            return account;  // Success
        }
    }

    // Get validated account with descriptive label
    // Includes retry loop for both format validation and account existence
    public Account getValidatedAccountWithLabel(String label, String errorMessage) {
        while (true) {
            String accNo = this.getValidatedInput(label,
                    ValidationPatterns.ACCOUNT_NO_PATTERN,
                    "(format: " + ValidationPatterns.ACCOUNT_NO_FORMAT + " e.g., ACC001)");
            if (accNo == null) return null;  // User cancelled

            Account account = AccountUtils.findAccount(this.accountList, accNo);
            if (account == null) {
                System.out.println(errorMessage);
                System.out.println("   Please try again or type 'back' to cancel.\n");
                continue;  // RETRY
            }
            return account;  // Success
        }
    }

    // Get validated amount with descriptive label
    public Double getValidatedAmountWithLabel(String label) {
        while (true) {
            System.out.print(label + " (positive number) or type 'back' to cancel: ");
            String input = this.sc.nextLine().trim();

            if (input.equalsIgnoreCase("back")) {
                System.out.println("✗ Operation cancelled. Returning to menu...\n");
                return null;
            }

            try {
                double amount = Double.parseDouble(input);
                if (amount > 0) {
                    return amount;
                } else {
                    System.out.println("✗ Amount must be greater than zero!");
                    System.out.println("   Please try again...\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("✗ Invalid number format");
                System.out.println("   Please try again...\n");
            }
        }
    }

    // Get validated customer with profile
    public Customer getValidatedCustomerWithProfile() {
        Customer customer = this.getValidatedCustomer(
                "✗ Customer not found. Cannot access profile.");
        if (customer == null) return null;

        if (customer.getProfile() == null) {
            System.out.println("✗ Customer has no profile. Please create one first.");
            return null;
        }
        return customer;
    }

    // Get validated account with access control
    public Account getValidatedAccountWithAccessControl(User currentUser) {
        // Check if user is null (no one logged in)
        if (currentUser == null) {
            System.out.println("✗ No user logged in");
            return null;
        }

        // If user is admin, allow any account input
        if (currentUser.getUserRole() == UserRole.ADMIN) {
            return this.getValidatedAccount("✗ Account not found");
        }

        // For customers: show their linked customer's accounts and let them choose
        if (currentUser instanceof UserAccount) {
            UserAccount customerAccount = (UserAccount) currentUser;
            String linkedCustomerId = customerAccount.getLinkedCustomerId();

            // Find all accounts owned by this customer
            LinkedList<Account> customerAccounts = new LinkedList<>();
            for (Account acc : this.accountList) {
                if (acc.getOwner() != null && acc.getOwner().getCustomerId().equals(linkedCustomerId)) {
                    customerAccounts.add(acc);
                }
            }

            // If customer has no accounts, inform them
            if (customerAccounts.isEmpty()) {
                System.out.println("✗ You have no accounts. Contact an administrator to create accounts.");
                return null;
            }

            // Show available accounts
            System.out.println("\nYour accounts:");
            int index = 1;
            for (com.banking.models.Account acc : customerAccounts) {
                System.out.println("  " + index + ". " + acc.getAccountNo() + " (" + acc.getDetails() + ")");
                index++;
            }

            // Retry loop for account selection
            while (true) {
                String accNo = this.getValidatedInput("Account Number",
                        ValidationPatterns.ACCOUNT_NO_PATTERN,
                        "(format: " + ValidationPatterns.ACCOUNT_NO_FORMAT + " e.g., ACC001)");
                if (accNo == null) return null;  // User cancelled

                // Verify the chosen account belongs to this customer
                for (com.banking.models.Account acc : customerAccounts) {
                    if (acc.getAccountNo().equals(accNo)) {
                        return acc;  // Success
                    }
                }

                // Not in customer's account list - retry
                System.out.println("✗ You can only access accounts that belong to you");
                System.out.println("   Please select one of your accounts listed above or type 'back' to cancel.\n");
                continue;  // RETRY
            }
        }

        System.out.println("✗ Cannot determine user type");
        return null;
    }

    // Prompt user for yes/no confirmation with validation and retry
    // Accepts: "yes"/"y" for true, "no"/"n" for false
    // Any other input (including "1", "2", etc.) shows error and retries
    public boolean confirmAction(String message) {
        while (true) {
            System.out.print(message + " (yes/no): ");
            String response = this.sc.nextLine().trim().toLowerCase();

            // Accept yes/y as true
            if (response.equals("yes") || response.equals("y")) {
                return true;
            }
            // Accept no/n as false
            else if (response.equals("no") || response.equals("n")) {
                return false;
            }
            // Invalid input - retry (including numeric like "1", "2")
            else {
                UIFormatter.printError("Invalid input. Please enter 'yes' or 'no'.");
                System.out.println("   Please try again...\n");
                continue;
            }
        }
    }

    // Safely log action with null-safety check
    public static void safeLogAction(com.banking.BankingSystem bankingSystem, String action, String details) {
        if (bankingSystem != null && bankingSystem.getCurrentUser() != null) {
            bankingSystem.logAction(action, details);
        }
    }

    // ===== ENHANCED INPUT VALIDATION METHODS (Phase 3 Improvements) =====

    /**
     * Enhanced confirmation with warning message displayed in styled box.
     * Provides better user feedback for critical operations.
     *
     * Example:
     * <pre>
     * ⚠ WARNING: This action cannot be undone
     *
     * ╔══════════════════════════════════════════════════════════════════╗
     * ║  ⚠ CONFIRMATION REQUIRED                                        ║
     * ╠══════════════════════════════════════════════════════════════════╣
     * ║  Are you sure you want to delete account ACC001?                ║
     * ╚══════════════════════════════════════════════════════════════════╝
     * → Your choice (yes/no): _
     * </pre>
     *
     * @param message Confirmation question
     * @param warningText Warning text to display before prompt (can be null)
     * @return true if user confirmed, false otherwise
     */
    public boolean confirmActionEnhanced(String message, String warningText) {
        // Display warning box once (before retry loop)
        if (warningText != null && !warningText.isEmpty()) {
            UIFormatter.printWarning(warningText);
        }

        // Display confirmation prompt once (before retry loop)
        UIFormatter.printConfirmationPrompt(message);

        // Validation and retry loop
        while (true) {
            String response = this.sc.nextLine().trim().toLowerCase();

            // Accept yes/y as true
            if (response.equals("yes") || response.equals("y")) {
                UIFormatter.printInfo("Action confirmed. Proceeding...");
                return true;
            }
            // Accept no/n as false
            else if (response.equals("no") || response.equals("n")) {
                UIFormatter.printInfo("Action cancelled.");
                return false;
            }
            // Invalid input - show error and retry
            else {
                UIFormatter.printError("Invalid input. Please enter 'yes' or 'no'.");
                System.out.println("   Please try again...\n");
                System.out.print("→ Your choice (yes/no): ");  // Re-prompt
                continue;
            }
        }
    }

    /**
     * Get validated input with real-time format feedback.
     * Shows checkmark when format is correct, error when incorrect.
     *
     * Example interaction:
     * <pre>
     * → Account Number (format: ACC###): xyz
     *   ✗ Invalid format! (format: ACC###)
     *    Please try again or type 'back' to cancel.
     *
     * → Account Number (format: ACC###): ACC001
     *   ✓ Format correct!
     * </pre>
     *
     * @param prompt Field name
     * @param pattern Regex pattern
     * @param formatHint Format hint text
     * @return Validated input or null if cancelled
     */
    public String getValidatedInputWithFeedback(String prompt, String pattern, String formatHint) {
        while (true) {
            System.out.print(UIFormatter.INFO + " " + prompt + " " + formatHint + ": ");
            String input = this.sc.nextLine().trim();

            if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("cancel")) {
                UIFormatter.printInfo("Operation cancelled. Returning to menu...");
                return null;
            }

            if (input.matches(pattern)) {
                System.out.println("  " + UIFormatter.SUCCESS + " Format correct!");
                return input;
            } else {
                System.out.println("  " + UIFormatter.ERROR + " Invalid format! " + formatHint);
                System.out.println("   Please try again or type 'back' to cancel.\n");
            }
        }
    }
}
