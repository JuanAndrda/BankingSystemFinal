package com.banking.managers;

import com.banking.models.*;
import com.banking.utilities.*;
import com.banking.BankingSystem;
import com.banking.auth.UserAccount;
import com.banking.managers.AuthenticationManager;
import java.util.*;

public class CustomerManager {
    private LinkedList<Customer> customers;
    private LinkedList<Account> accountList;
    private InputValidator validator;
    private BankingSystem bankingSystem;  // Reference for permission checks and audit logging
    private AccountManager accountMgr;    // Reference for account creation in onboarding workflow
    private int customerIdCounter = 1;    // Auto-generate Customer IDs (C001, C002, etc.)

    public CustomerManager(LinkedList<Customer> customers, LinkedList<Account> accountList, InputValidator validator) {
        this.customers = customers;
        this.accountList = accountList;
        this.validator = validator;
        this.bankingSystem = null;  // Set later via setBankingSystem()
        this.accountMgr = null;     // Set later via setAccountManager()
    }


    public String generateNextCustomerId() {
        // Scan all customers to find highest ID number
        int maxId = 0;
        for (Customer c : this.customers) {
            String id = c.getCustomerId();
            if (id.startsWith("C") && id.length() == 4) {
                try {
                    // Extract numeric part: C001 -> 001 -> 1
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxId) {
                        maxId = num;  // Track highest number found
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid IDs (safety check)
                }
            }
        }

        // Next ID is maxId + 1 (ensures no collisions even with gaps)
        this.customerIdCounter = maxId + 1;
        return "C" + String.format("%03d", this.customerIdCounter++);
    }

    public String generateNextProfileId() {
        int maxId = 0;

        // Find the highest existing profile ID number
        for (Customer c : this.customers) {
            if (c.getProfile() != null) {
                String profileId = c.getProfile().getProfileId();
                if (profileId != null && profileId.startsWith("P") && profileId.length() == 4) {
                    try {
                        int num = Integer.parseInt(profileId.substring(1));
                        if (num > maxId) {
                            maxId = num;
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid profile IDs
                    }
                }
            }
        }

        // Return next profile ID
        return "P" + String.format("%03d", maxId + 1);
    }


    public Customer createCustomer(String customerId, String name) {
        // Note: Duplicate validation removed - auto-generation (maxId + 1) guarantees uniqueness
        // If manual IDs are ever supported, add validation here

        try {
            Customer c = new Customer(customerId, name);
            this.customers.add(c);
            UIFormatter.printSuccess("Customer created: " + c);
            return c;
        } catch (IllegalArgumentException e) {
            UIFormatter.printError("Error creating customer: " + e.getMessage());
            return null;
        }
    }

    public Customer findCustomer(String customerId) {
        for (Customer c : this.customers) {
            if (c.getCustomerId().equals(customerId)) {
                return c;
            }
        }
        return null;
    }

    public boolean validateCustomerExists(String customerId) {
        return this.findCustomer(customerId) != null;
    }

    public boolean deleteCustomer(String customerId) {
        // Calls this.findCustomer() to search LinkedList
        Customer customer = this.findCustomer(customerId);
        if (customer == null) {
            UIFormatter.printError("Customer not found: " + customerId);
            return false;
        }

        // Remove all accounts associated with this customer (cascading delete)
        // Create copy to avoid ConcurrentModificationException during iteration
        LinkedList<Account> accountsCopy = new LinkedList<>(customer.getAccounts());
        boolean allAccountsDeleted = true;
        for (Account acc : accountsCopy) {
            // Calls this.deleteAccount() to remove from system and customer
            boolean accDeleted = this.deleteAccount(acc.getAccountNo());
            if (!accDeleted) {
                System.out.println("⚠ Warning: Failed to delete account " + acc.getAccountNo() + " for customer " + customerId);
                allAccountsDeleted = false;
            }
        }
        if (!allAccountsDeleted) {
            System.out.println("⚠ Warning: Not all accounts were successfully deleted");
        }

        // Remove customer from LinkedList
        this.customers.remove(customer);
        UIFormatter.printSuccess("Customer deleted: " + customerId);
        return true;
    }


    public boolean createOrUpdateProfile(String custId, String profileId, String address, String phone, String email, boolean allowReplace) {
        Customer customer = this.findCustomer(custId);
        if (customer == null) {
            UIFormatter.printError("Customer not found");
            return false;
        }

        // Check if profile already exists
        if (customer.getProfile() != null && !allowReplace) {
            UIFormatter.printError("Customer already has a profile. Use update instead.");
            return false;
        }

        // Check for duplicate profile ID (but allow if replacing customer's own profile)
        if (this.validator.profileIdExists(profileId) &&
            (customer.getProfile() == null || !customer.getProfile().getProfileId().equals(profileId))) {
            UIFormatter.printError("Profile ID already exists: " + profileId);
            return false;
        }

        try {
            CustomerProfile profile = new CustomerProfile(profileId, address, phone, email);
            customer.setProfile(profile);
            UIFormatter.printSuccess("Profile created/updated for customer: " + customer.getName());
            System.out.println(profile);

            // Verify bidirectional relationship (demonstrates one-to-one)
            if (profile.getCustomer() != null && profile.getCustomer().equals(customer)) {
                UIFormatter.printInfo("One-to-one relationship successfully established!");
            }
            return true;
        } catch (IllegalArgumentException e) {
            UIFormatter.printError("Error creating profile: " + e.getMessage());
            return false;
        }
    }

    public void handleCreateCustomerProfile() {
        UIFormatter.printSectionHeader("CREATE CUSTOMER PROFILE");
        String custId = this.validator.getValidatedInput("Customer ID (to link profile to):",
                ValidationPatterns.CUSTOMER_ID_PATTERN,
                "(format: " + ValidationPatterns.CUSTOMER_ID_FORMAT + " e.g., C001)");
        if (custId == null) return;

        Customer customer = this.findCustomer(custId);
        if (customer == null) {
            UIFormatter.printError("Customer not found");
            return;
        }

        // Check if profile already exists
        if (customer.getProfile() != null) {
            System.out.println("⚠ This customer already has a profile.");
            if (!this.validator.confirmAction("Do you want to replace it?")) {
                UIFormatter.printInfo("Operation cancelled.");
                return;
            }
        }

        // Auto-generate Profile ID (like account numbers)
        String profileId = this.generateNextProfileId();
        UIFormatter.printSuccess("Profile ID auto-generated: " + profileId);

        // Get profile details using unified helper
        String[] profileDetails = this.promptForProfileDetails();
        if (profileDetails == null) return;

        // Create or update profile
        this.createOrUpdateProfile(custId, profileId, profileDetails[0], profileDetails[1], profileDetails[2], true);
    }

    public void handleUpdateCustomerProfile() {
        UIFormatter.printSectionHeader("UPDATE CUSTOMER PROFILE");

        Customer customer = this.validator.getValidatedCustomerWithProfile();
        if (customer == null) return;

        CustomerProfile profile = customer.getProfile();
        System.out.println("\nCurrent Profile:");
        UIFormatter.printDataRow("Address:", profile.getAddress());
        UIFormatter.printDataRow("Phone:", profile.getPhone());
        UIFormatter.printDataRow("Email:", profile.getEmail());

        // Retry loop for menu choice
        while (true) {
            System.out.println("\n--- What would you like to update? ---");
            System.out.println("1. Address");
            System.out.println("2. Phone");
            System.out.println("3. Email");
            System.out.println("0. Cancel");
            UIFormatter.printPrompt("Choice: ");

            Scanner sc = this.validator.getScanner();
            String input = sc.nextLine().trim();

            // Handle cancellation
            if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("cancel")) {
                UIFormatter.printInfo("Update cancelled");
                return;
            }

            // Parse and validate choice
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                UIFormatter.printError("Invalid input. Please enter 1, 2, 3, or 0.");
                System.out.println("   Please try again...\n");
                continue;  // RETRY
            }

            switch (choice) {
                case 1:
                    String newAddress = this.validator.getValidatedString("New Address");
                    if (newAddress != null) {
                        profile.setAddress(newAddress);
                        UIFormatter.printSuccess("Address updated");
                    }
                    return;  // Exit after update
                case 2:
                    String newPhone = this.validator.getValidatedPhoneNumber("New Phone (min 10 digits)");
                    if (newPhone != null) {
                        profile.setPhone(newPhone);
                        UIFormatter.printSuccess("Phone updated");
                    }
                    return;  // Exit after update
                case 3:
                    String newEmail = this.validator.getValidatedEmail("New Email");
                    if (newEmail != null) {
                        profile.setEmail(newEmail);
                        UIFormatter.printSuccess("Email updated");
                    }
                    return;  // Exit after update
                case 0:
                    UIFormatter.printInfo("Update cancelled");
                    return;
                default:
                    UIFormatter.printError("Invalid choice. Please enter 1, 2, 3, or 0.");
                    System.out.println("   Please try again...\n");
                    continue;  // RETRY
            }
        }
    }


    public void handleCreateCustomer() {
        UIFormatter.printSectionHeader("CREATE CUSTOMER (Auto-Generation)");

        // Step 1: Get customer name (only required input)
        // Calls InputValidator.getValidatedString() from this.validator
        String custName = this.validator.getValidatedString("Customer Name");
        if (custName == null) return;

        // Step 2: Auto-generate Customer ID
        // Calls this.generateNextCustomerId() to create unique ID (C###)
        String custId = this.generateNextCustomerId();
        UIFormatter.printSuccess("Auto-generated Customer ID: " + custId);

        // Step 3: Create the Customer in the system
        // Calls this.createCustomer() to add customer to LinkedList
        Customer newCustomer = this.createCustomer(custId, custName);
        if (newCustomer == null) {
            UIFormatter.printError("Failed to create customer");
            return;
        }

        // Step 4: Get AuthenticationManager for username/password generation
        // Calls BankingSystem.getAuthenticationManager()
        AuthenticationManager authManager = this.bankingSystem.getAuthenticationManager();

        // Step 5: Auto-generate username and password
        // Calls AuthenticationManager.generateUsername() - converts "John Doe" to "john_doe"
        String username = authManager.generateUsername(custName);
        // Calls AuthenticationManager.generateTemporaryPassword() - format: "xx####"
        String tempPassword = authManager.generateTemporaryPassword(username);

        // Step 6: Link UserAccount (linked to this customer)
        // Calls BankingSystem.registerUser() to add to user registry
        UserAccount userAccount = new UserAccount(username, tempPassword, custId);
        if (!this.bankingSystem.registerUser(userAccount)) {
            UIFormatter.printError("Failed to create login account for customer");
            return;
        }

        UIFormatter.printBlankLine();
        UIFormatter.printBoxTitle("CUSTOMER CREATION SUCCESSFUL");
        UIFormatter.printSuccess("Customer Information:",
                "Customer ID:     " + custId,
                "Customer Name:   " + custName);
        UIFormatter.printSuccess("Login Credentials (System Generated):",
                "Username:        " + username,
                "Temp Password:   " + tempPassword,
                "Note: Customer MUST change password on first login");

        // Log the action
        InputValidator.safeLogAction(bankingSystem, "CREATE_CUSTOMER",
                "Customer ID: " + custId + " Name: " + custName + " Username: " + username);

        // Step 7-10: Integrated Onboarding Workflow
        boolean profileCreated = this.promptAndCreateProfile(newCustomer);

        boolean accountCreated = false;
        if (profileCreated) {
            accountCreated = this.promptAndCreateAccount(newCustomer);
        }

        // Display Onboarding Summary
        UIFormatter.printBlankLine();
        UIFormatter.printBoxTitle("ONBOARDING COMPLETE");
        System.out.println();
        UIFormatter.printBulletLine("Customer: " + custName + " (" + custId + ")");
        if (profileCreated) {
            String profileId = newCustomer.getProfile() != null ? newCustomer.getProfile().getProfileId() : "N/A";
            UIFormatter.printBulletLine("Profile: " + profileId + " ✓ CREATED");
        } else {
            UIFormatter.printBulletLine("Profile: SKIPPED");
        }
        if (accountCreated) {
            UIFormatter.printBulletLine("Account: CREATED ✓");
        } else {
            UIFormatter.printBulletLine("Account: NOT CREATED (Optional)");
        }
        System.out.println("\nCustomer is ready to use the banking system!\n");
    }

    public void handleViewCustomerDetails() {
        UIFormatter.printSectionHeader("VIEW CUSTOMER DETAILS");

        Customer customer = this.validator.getValidatedCustomer(
                "✗ Customer not found. Please check the Customer ID.");
        if (customer == null) return;

        // Professional box format for customer details
        System.out.println();
        UIFormatter.printTopBorder();
        UIFormatter.printCenteredLine("CUSTOMER INFORMATION");
        UIFormatter.printMiddleBorder();

        UIFormatter.printLeftAlignedLine("  Customer ID:        " + customer.getCustomerId(), 0);
        UIFormatter.printLeftAlignedLine("  Name:               " + customer.getName(), 0);

        // Profile information
        CustomerProfile profile = customer.getProfile();
        if (profile != null) {
            UIFormatter.printLeftAlignedLine("  Address:            " + profile.getAddress(), 0);
            UIFormatter.printLeftAlignedLine("  Phone:              " + profile.getPhone(), 0);
            UIFormatter.printLeftAlignedLine("  Email:              " + profile.getEmail(), 0);
        } else {
            UIFormatter.printLeftAlignedLine("  Profile:            Not created", 0);
        }

        UIFormatter.printMiddleBorder();
        UIFormatter.printLeftAlignedLine("ASSOCIATED ACCOUNTS", 2);
        UIFormatter.printMiddleBorder();

        LinkedList<Account> accounts = customer.getAccounts();
        if (accounts.isEmpty()) {
            UIFormatter.printLeftAlignedLine("  No accounts. Use option 5 (Create Account) to add one.", 2);
        } else {
            for (Account acc : accounts) {
                String accInfo = String.format("  %s %s (%s) - Balance: $%.2f",
                        UIFormatter.BULLET,
                        acc.getAccountNo(),
                        acc instanceof SavingsAccount ? "Savings" : "Checking",
                        acc.getBalance());
                UIFormatter.printLeftAlignedLine(accInfo, 0);
            }
        }

        UIFormatter.printBottomBorder();
        System.out.println();
    }

    public void handleViewAllCustomers() {
        UIFormatter.printSectionHeader("VIEW ALL CUSTOMERS");

        if (this.customers.isEmpty()) {
            UIFormatter.printInfo("No customers found. Use option 1 (Create Customer) to add one.");
            return;
        }

        System.out.println("\nTotal Customers: " + this.customers.size());
        System.out.println();

        // Professional table format
        UIFormatter.printTableHeader("Customer ID", "Name", "Accounts", "Total Balance");

        for (Customer customer : this.customers) {
            String customerId = customer.getCustomerId();
            String name = customer.getName();
            String accountCount = String.valueOf(customer.getAccounts().size());

            // Calculate total balance across all accounts
            double totalBalance = 0.0;
            for (Account acc : customer.getAccounts()) {
                totalBalance += acc.getBalance();
            }
            String balance = "$" + String.format("%.2f", totalBalance);

            UIFormatter.printTableRow(customerId, name, accountCount, balance);
        }

        UIFormatter.printTableFooter();
        System.out.println();
    }

    public void handleDeleteCustomer() {
        UIFormatter.printSectionHeader("DELETE CUSTOMER");

        Customer customer = null;
        while (customer == null) {  // RETRY LOOP for customer lookup
            String custId = this.validator.getValidatedInputWithFeedback(
                    "Customer ID:",
                    com.banking.utilities.ValidationPatterns.CUSTOMER_ID_PATTERN,
                    "(format: " + com.banking.utilities.ValidationPatterns.CUSTOMER_ID_FORMAT + " e.g., C001)");
            if (custId == null) return;  // User cancelled - exit immediately

            customer = this.findCustomer(custId);
            if (customer == null) {
                UIFormatter.printErrorEnhanced(
                        "Customer does not exist",
                        "Use 'View All Customers' to see valid customer IDs.");

                if (!this.validator.confirmAction("Try again?")) {
                    return;  // User chose no - exit
                }
                // User chose yes - retry (loop continues)
            }
        }

        int accountCount = customer.getAccounts().size();

        // Show customer details before deletion
        System.out.println("\nCustomer to be deleted:");
        UIFormatter.printDataRow("  Customer ID:", customer.getCustomerId());
        UIFormatter.printDataRow("  Name:", customer.getName());
        UIFormatter.printDataRow("  Associated Accounts:", String.valueOf(accountCount));
        System.out.println();

        // Enhanced confirmation with warning
        String warningText = (accountCount > 0)
                ? "This will also delete all " + accountCount + " associated account(s) and transaction history."
                : null;

        if (!this.validator.confirmActionEnhanced(
                "Are you sure you want to delete customer " + customer.getCustomerId() + "?",
                warningText)) {
            return;
        }

        UIFormatter.printLoading("Deleting customer and associated accounts");
        boolean deleted = this.deleteCustomer(customer.getCustomerId());

        if (deleted) {
            UIFormatter.printSuccessEnhanced(
                    "Customer deleted successfully!",
                    "Customer ID: " + customer.getCustomerId(),
                    "Name: " + customer.getName(),
                    "Associated Accounts Deleted: " + accountCount,
                    "Status: Deleted");
            InputValidator.safeLogAction(bankingSystem, "DELETE_CUSTOMER", "Customer ID: " + customer.getCustomerId());
        } else {
            UIFormatter.printErrorEnhanced(
                    "Failed to delete customer",
                    "Please try again or contact support.");
        }
    }


    private String[] promptForProfileDetails() {
        String address = this.validator.getValidatedString("Address");
        if (address == null) return null;

        String phone = this.validator.getValidatedPhoneNumber("Phone Number (min 10 digits)");
        if (phone == null) return null;

        String email = this.validator.getValidatedEmail("Email");
        if (email == null) return null;

        return new String[]{address, phone, email};
    }

    private boolean createAndLinkProfile(Customer customer, String profileId, String address, String phone, String email) {
        try {
            CustomerProfile profile = new CustomerProfile(profileId, address, phone, email);
            customer.setProfile(profile);
            UIFormatter.printSuccess("Profile created and linked to customer!",
                    "Profile ID: " + profileId,
                    "Email: " + email);
            return true;
        } catch (IllegalArgumentException e) {
            UIFormatter.printError("Error creating profile: " + e.getMessage());
            return false;
        }
    }


    public boolean promptAndCreateProfile(Customer customer) {
        System.out.println("\n--- NEXT STEP: CREATE PROFILE ---");

        // Auto-generate next profile ID
        String profileId = this.generateNextProfileId();
        UIFormatter.printSuccess("Profile ID auto-generated: " + profileId);

        // Get profile details using unified helper
        String[] profileDetails = this.promptForProfileDetails();
        if (profileDetails == null) {
            UIFormatter.printError("Profile creation cancelled.");
            return false;
        }

        // Create and link profile using unified helper
        return this.createAndLinkProfile(customer, profileId, profileDetails[0], profileDetails[1], profileDetails[2]);
    }

    public boolean promptAndCreateAccount(Customer customer) {
        System.out.println("\n--- NEXT STEP: CREATE ACCOUNT (Optional) ---");

        // Ask if they want to create account
        if (!this.validator.confirmAction("Would you like to create an account for this customer now?")) {
            UIFormatter.printInfo("Skipping account creation. You can create accounts later from the menu.");
            return false;
        }

        // Retry loop for menu choice
        String accountType = null;
        while (accountType == null) {
            // Show account type menu
            System.out.println("\nSelect Account Type:");
            UIFormatter.printBulletLine("1. Savings (3.0% interest)");
            UIFormatter.printBulletLine("2. Checking ($500 overdraft)");
            System.out.print(UIFormatter.INFO + " Enter choice (1 or 2) or 'back' to cancel: ");

            try {
                String input = this.validator.getScanner().nextLine().trim();

                // Handle cancellation
                if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("cancel")) {
                    UIFormatter.printInfo("Account creation cancelled.");
                    return false;
                }

                // Parse and validate choice
                int choice;
                try {
                    choice = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    UIFormatter.printError("Invalid input. Please enter 1 or 2.");
                    System.out.println("   Please try again...\n");
                    continue;  // Retry
                }

                if (choice == 1) {
                    accountType = "SAVINGS";
                } else if (choice == 2) {
                    accountType = "CHECKING";
                } else {
                    UIFormatter.printError("Invalid choice. Please select 1 or 2.");
                    System.out.println("   Please try again...\n");
                    continue;  // Retry
                }
            } catch (Exception e) {
                UIFormatter.printError("Error reading input: " + e.getMessage());
                System.out.println("   Please try again...\n");
                continue;  // Retry
            }
        }

        // Create account using unified helper (auto-generates number, displays result, logs action)
        try {
            Account createdAccount = this.accountMgr.createAndDisplayAccount(customer, accountType);
            return createdAccount != null;
        } catch (Exception e) {
            UIFormatter.printError("Error creating account: " + e.getMessage());
            return false;
        }
    }


    private boolean deleteAccount(String accountNo) {
        Account account = AccountUtils.findAccount(this.accountList, accountNo);
        if (account == null) {
            return false;
        }

        this.accountList.remove(account);
        Customer owner = account.getOwner();
        if (owner == null) {
            System.out.println("⚠ Warning: Account had no owner");
            return false;
        }
        boolean removedFromCustomer = owner.removeAccount(accountNo);
        if (!removedFromCustomer) {
            System.out.println("⚠ Warning: Account was removed from system but not from customer record");
            return false;
        }
        return true;
    }


    public void setBankingSystem(BankingSystem bankingSystem) {
        this.bankingSystem = bankingSystem;
    }

    public void setAccountManager(AccountManager accountMgr) {
        this.accountMgr = accountMgr;
    }

}
