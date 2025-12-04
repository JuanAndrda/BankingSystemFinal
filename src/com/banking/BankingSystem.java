package com.banking;

import com.banking.models.*;
import com.banking.managers.*;
import com.banking.auth.*;
import com.banking.utilities.*;
import com.banking.menu.MenuBuilder;
import java.util.*;

/**
 * Banking Management System - Main Controller Class
 *
 * This class demonstrates BOTH types of POLYMORPHISM:
 *
 * 1. COMPILE-TIME POLYMORPHISM (Method Overloading):
 *    - Account.withdraw() and other overloaded methods in manager classes
 *    - The compiler decides which method to call based on the arguments.
 *
 * 2. RUNTIME POLYMORPHISM (Method Override):
 *    - Account.withdraw() is overridden by SavingsAccount.withdraw() and CheckingAccount.withdraw()
 *    - Account.getDetails() is overridden by SavingsAccount.getDetails() and CheckingAccount.getDetails()
 *    - The JVM decides which method to call based on the actual object type at runtime.
 *
 * INHERITANCE is demonstrated through:
 *    - Account (parent) → SavingsAccount, CheckingAccount (children)
 *
 * ABSTRACTION is demonstrated through:
 *    - Account is abstract, cannot be instantiated directly
 *    - Must create SavingsAccount or CheckingAccount
 *
 * COMPOSITION is demonstrated through:
 *    - BankingSystem HAS-A InputValidator (NOT IS-A)
 *    - BankingSystem HAS-A CustomerManager
 *    - BankingSystem HAS-A AccountManager
 *    - BankingSystem HAS-A TransactionProcessor
 *
 * This refactoring shows the SINGLE RESPONSIBILITY PRINCIPLE:
 *    - BankingSystem: Orchestrates and manages main menu
 *    - InputValidator: All input validation logic
 *    - CustomerManager: All customer operations
 *    - AccountManager: All account operations
 *    - TransactionProcessor: All transaction operations
 */
public class BankingSystem {
    // ===== SHARED DATA COLLECTIONS =====
    private LinkedList<Customer> customers;
    private LinkedList<Account> accountList;

    // ===== MANAGER OBJECTS (COMPOSITION) =====
    private InputValidator validator;
    private CustomerManager customerMgr;
    private AccountManager accountMgr;
    private TransactionProcessor txProcessor;
    private AuthenticationManager authManager;  // Handles login and permissions

    // ===== I/O RESOURCE =====
    private Scanner sc;  // Scanner for user input (injected via constructor)
    private User currentUser;  // Currently logged-in user

    /**
     * Constructor: Initialize all manager objects with shared collections and Scanner.
     * Uses Constructor Injection pattern for Scanner (Dependency Injection).
     *
     * @param sc Scanner instance for user input (required for interactive menu)
     */
    public BankingSystem(Scanner sc) {
        // Store injected Scanner
        this.sc = sc;

        // Initialize shared collections
        this.customers = new LinkedList<>();
        this.accountList = new LinkedList<>();

        // Initialize manager objects with shared collections and Scanner
        this.validator = new InputValidator(this.customers, this.accountList, sc);
        this.customerMgr = new CustomerManager(this.customers, this.accountList, this.validator);
        this.accountMgr = new AccountManager(this.customers, this.accountList, this.validator);
        this.txProcessor = new TransactionProcessor(this.accountList, this.validator);

        // CIRCULAR DEPENDENCY RESOLUTION:
        // Why we use setBankingSystem() instead of passing 'this' in constructor:
        //
        // Problem: Managers need BankingSystem reference for:
        //   - canAccessAccount() - security checks
        //   - logAction() - audit trail logging
        //   - getCurrentUser() - get logged-in user
        //
        // If we passed 'this' in constructor:
        //   new CustomerManager(..., this)  // ❌ Would work, but fragile
        //
        // Issues with constructor approach:
        //   1. 'this' escapes before BankingSystem is fully constructed
        //   2. If manager calls bankingSystem.method() in constructor, authManager might be null
        //   3. Order of initialization matters - hard to maintain
        //
        // Solution: Two-phase initialization
        //   Phase 1: Create all manager objects (no circular refs)
        //   Phase 2: Wire up circular references via setters (safe, all objects exist)
        //
        // This is a common pattern in enterprise Java (Spring Framework uses this)
        this.customerMgr.setBankingSystem(this);
        this.customerMgr.setAccountManager(this.accountMgr);  // For onboarding workflow
        this.accountMgr.setBankingSystem(this);
        this.txProcessor.setBankingSystem(this);

        // Initialize authentication manager (no circular dependency)
        this.authManager = new AuthenticationManager();
        this.currentUser = null;
    }

    // ===== ROLE-BASED MENU DISPLAY METHODS =====
    /**
     * Displays the admin menu with all available options using professional formatting.
     * Uses UIFormatter utility class for consistent, professional appearance.
     * Numbers come from MenuAction enum for single source of truth.
     *
     * Shows:
     * - Customer operations (create, view, update, delete)
     * - Account operations (create, view, manage, delete)
     * - Transaction operations (deposit, withdraw, transfer, history)
     * - Profile operations (create, update profiles)
     * - Reports & utilities (interest, sorting, audit trail)
     * - Session management (logout, exit, change password)
     *
     * Called by: this.runConsoleMenu() based on current user's role
     */
    private void displayAdminMenu() {
        UserRole role = UserRole.ADMIN;

        // Title
        UIFormatter.printBoxTitle("BANKING MANAGEMENT SYSTEM");
        UIFormatter.printBlankLine();

        // Customer Operations
        String[] customerOps = {
            "[" + MenuAction.CREATE_CUSTOMER.getMenuNumber(role) + "] " + MenuAction.CREATE_CUSTOMER.getDisplayName(),
            "[" + MenuAction.VIEW_CUSTOMER_DETAILS.getMenuNumber(role) + "] " + MenuAction.VIEW_CUSTOMER_DETAILS.getDisplayName(),
            "[" + MenuAction.VIEW_ALL_CUSTOMERS.getMenuNumber(role) + "] " + MenuAction.VIEW_ALL_CUSTOMERS.getDisplayName(),
            "[" + MenuAction.DELETE_CUSTOMER.getMenuNumber(role) + "] " + MenuAction.DELETE_CUSTOMER.getDisplayName()
        };
        UIFormatter.printMenuSection("CUSTOMER OPERATIONS", customerOps);

        // Account Operations
        String[] accountOps = {
            "[" + MenuAction.CREATE_ACCOUNT.getMenuNumber(role) + "] " + MenuAction.CREATE_ACCOUNT.getDisplayName(),
            "[" + MenuAction.VIEW_ACCOUNT_DETAILS.getMenuNumber(role) + "] " + MenuAction.VIEW_ACCOUNT_DETAILS.getDisplayName(),
            "[" + MenuAction.VIEW_ALL_ACCOUNTS.getMenuNumber(role) + "] " + MenuAction.VIEW_ALL_ACCOUNTS.getDisplayName(),
            "[" + MenuAction.DELETE_ACCOUNT.getMenuNumber(role) + "] " + MenuAction.DELETE_ACCOUNT.getDisplayName(),
            "[" + MenuAction.UPDATE_OVERDRAFT_LIMIT.getMenuNumber(role) + "] " + MenuAction.UPDATE_OVERDRAFT_LIMIT.getDisplayName()
        };
        UIFormatter.printMenuSection("ACCOUNT OPERATIONS", accountOps);

        // Transaction Operations
        String[] transactionOps = {
            "[" + MenuAction.DEPOSIT_MONEY.getMenuNumber(role) + "] " + MenuAction.DEPOSIT_MONEY.getDisplayName(),
            "[" + MenuAction.WITHDRAW_MONEY.getMenuNumber(role) + "] " + MenuAction.WITHDRAW_MONEY.getDisplayName(),
            "[" + MenuAction.TRANSFER_MONEY.getMenuNumber(role) + "] " + MenuAction.TRANSFER_MONEY.getDisplayName(),
            "[" + MenuAction.VIEW_TRANSACTION_HISTORY.getMenuNumber(role) + "] " + MenuAction.VIEW_TRANSACTION_HISTORY.getDisplayName()
        };
        UIFormatter.printMenuSection("TRANSACTION OPERATIONS", transactionOps);

        // Profile Operations
        String[] profileOps = {
            "[" + MenuAction.CREATE_CUSTOMER_PROFILE.getMenuNumber(role) + "] " + MenuAction.CREATE_CUSTOMER_PROFILE.getDisplayName(),
            "[" + MenuAction.UPDATE_PROFILE_INFORMATION.getMenuNumber(role) + "] " + MenuAction.UPDATE_PROFILE_INFORMATION.getDisplayName()
        };
        UIFormatter.printMenuSection("PROFILE OPERATIONS", profileOps);

        // Reports & Utilities
        String[] reportOps = {
            "[" + MenuAction.APPLY_INTEREST.getMenuNumber(role) + "] " + MenuAction.APPLY_INTEREST.getDisplayName(),
            "[" + MenuAction.SORT_ACCOUNTS_BY_NAME.getMenuNumber(role) + "] " + MenuAction.SORT_ACCOUNTS_BY_NAME.getDisplayName(),
            "[" + MenuAction.SORT_ACCOUNTS_BY_BALANCE.getMenuNumber(role) + "] " + MenuAction.SORT_ACCOUNTS_BY_BALANCE.getDisplayName(),
            "[" + MenuAction.VIEW_AUDIT_TRAIL.getMenuNumber(role) + "] " + MenuAction.VIEW_AUDIT_TRAIL.getDisplayName()
        };
        UIFormatter.printMenuSection("REPORTS & UTILITIES", reportOps);

        // Session Management
        String[] sessionOps = {
            "[" + MenuAction.LOGOUT.getMenuNumber(role) + "] " + MenuAction.LOGOUT.getDisplayName(),
            "[" + MenuAction.EXIT_APPLICATION.getMenuNumber(role) + "] " + MenuAction.EXIT_APPLICATION.getDisplayName(),
            "[" + MenuAction.CHANGE_PASSWORD.getMenuNumber(role) + "] " + MenuAction.CHANGE_PASSWORD.getDisplayName()
        };
        UIFormatter.printMenuSection("SESSION MANAGEMENT", sessionOps);
    }

    /**
     * Displays the customer menu with limited options (ATM mode).
     * Uses UIFormatter utility class for consistent, professional appearance.
     * Numbers come from MenuAction enum - shows SEQUENTIAL 1-7, 0 (not 6, 10-13, 21)!
     * This is the key improvement - customers now see clean numbering.
     *
     * Called by: this.runConsoleMenu() based on current user's role
     */
    private void displayCustomerMenu() {
        UserRole role = UserRole.CUSTOMER;

        // Title
        UIFormatter.printBoxTitle("TRANSACTION MENU (ATM Mode)");
        UIFormatter.printBlankLine();

        // Account Operations
        String[] accountOps = {
            "[" + MenuAction.VIEW_ACCOUNT_DETAILS.getMenuNumber(role) + "] " + MenuAction.VIEW_ACCOUNT_DETAILS.getDisplayName()
        };
        UIFormatter.printMenuSection("ACCOUNT OPERATIONS", accountOps);

        // Transaction Operations
        String[] transactionOps = {
            "[" + MenuAction.DEPOSIT_MONEY.getMenuNumber(role) + "] " + MenuAction.DEPOSIT_MONEY.getDisplayName(),
            "[" + MenuAction.WITHDRAW_MONEY.getMenuNumber(role) + "] " + MenuAction.WITHDRAW_MONEY.getDisplayName(),
            "[" + MenuAction.TRANSFER_MONEY.getMenuNumber(role) + "] " + MenuAction.TRANSFER_MONEY.getDisplayName(),
            "[" + MenuAction.VIEW_TRANSACTION_HISTORY.getMenuNumber(role) + "] " + MenuAction.VIEW_TRANSACTION_HISTORY.getDisplayName()
        };
        UIFormatter.printMenuSection("TRANSACTION OPERATIONS", transactionOps);

        // Security Operations
        String[] securityOps = {
            "[" + MenuAction.CHANGE_PASSWORD.getMenuNumber(role) + "] " + MenuAction.CHANGE_PASSWORD.getDisplayName()
        };
        UIFormatter.printMenuSection("SECURITY OPERATIONS", securityOps);

        // Session Management
        String[] sessionOps = {
            "[" + MenuAction.LOGOUT.getMenuNumber(role) + "] " + MenuAction.LOGOUT.getDisplayName(),
            "[" + MenuAction.EXIT_APPLICATION.getMenuNumber(role) + "] " + MenuAction.EXIT_APPLICATION.getDisplayName()
        };
        UIFormatter.printMenuSection("SESSION MANAGEMENT", sessionOps);
    }

    // ===== MAIN ENTRY POINT: CONSOLE MENU =====
    /**
     * Runs the interactive console menu for the Banking System.
     * Displays role-based menu options with dual numbering system.
     * Uses MenuAction enum for type-safe, role-aware menu handling.
     * Scanner is injected via constructor (Dependency Injection).
     *
     * KEY IMPROVEMENTS:
     * - DUAL NUMBERING: Customers see 1-7, 0 | Admins see 1-21, 0
     * - SINGLE PERMISSION CHECK: No repeated role checks in switch statement
     * - TYPE SAFETY: Switch on MenuAction enum (not magic numbers)
     * - ROLE-AWARE LOOKUP: fromMenuNumber(int, UserRole) maps input correctly
     *
     * OOP Principles Demonstrated:
     * - ENCAPSULATION: Menu logic centralized in MenuAction enum
     * - ABSTRACTION: Complex dual numbering hidden behind simple interface
     * - POLYMORPHISM: Same code handles both roles differently
     * - FACADE PATTERN: Simplified interface to complex subsystems
     *
     * @return MenuAction indicating whether user chose LOGOUT or EXIT_APPLICATION
     */
    public MenuAction runConsoleMenu() {
        // Require login before accessing menu
        User loggedInUser = this.login();
        if (loggedInUser == null) {
            System.out.println("✗ Unable to proceed without authentication.");
            return MenuAction.EXIT_APPLICATION;  // Exit after failed login attempts
        }

        // Scanner already initialized from constructor (via Dependency Injection)
        boolean running = true;

        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║   BANKING MANAGEMENT SYSTEM v2.0   ║");
        System.out.println("╚════════════════════════════════════╝\n");

        while (running) {
            System.out.println("\n┌─ MAIN MENU ─────────────────────────────────────");

            // Display menu based on user role using MenuBuilder (auto-generated from MenuAction enum)
            String menuTitle = (this.currentUser.getUserRole() == UserRole.ADMIN)
                ? "BANKING MANAGEMENT SYSTEM"
                : "TRANSACTION MENU (ATM Mode)";
            MenuBuilder.displayMenu(this.currentUser.getUserRole(), menuTitle);

            System.out.println("└─────────────────────────────────────────────────");
            System.out.print("→ Enter choice: ");

            try {
                int choice = this.sc.nextInt();
                this.sc.nextLine(); // ⚠️ CRITICAL: Consume leftover newline

                // KEY IMPROVEMENT: Convert user input to MenuAction (ROLE-AWARE!)
                // Customer entering "2" → DEPOSIT_MONEY
                // Admin entering "2" → VIEW_CUSTOMER_DETAILS
                MenuAction action = MenuAction.fromMenuNumber(choice, this.currentUser.getUserRole());

                // Validate menu option exists
                if (action == null) {
                    System.out.println("\n✗ Invalid choice. Please try again.");
                    continue;
                }

                // SINGLE PERMISSION CHECK (no repeated checks in switch!)
                if (!action.canAccess(this.currentUser.getUserRole())) {
                    System.out.println("\n✗ Option not available for your role.");
                    continue;
                }

                System.out.println(); // Add blank line for readability

                // CLEAN SWITCH - Type-safe, no role checks needed!
                switch (action) {
                    // ===== CUSTOMER OPERATIONS (Admin Only) =====
                    case CREATE_CUSTOMER:
                        this.customerMgr.handleCreateCustomer();
                        break;

                    case VIEW_CUSTOMER_DETAILS:
                        this.customerMgr.handleViewCustomerDetails();
                        break;

                    case VIEW_ALL_CUSTOMERS:
                        this.customerMgr.handleViewAllCustomers();
                        break;

                    case DELETE_CUSTOMER:
                        this.customerMgr.handleDeleteCustomer();
                        break;

                    // ===== ACCOUNT OPERATIONS =====
                    case CREATE_ACCOUNT:
                        this.accountMgr.handleCreateAccount();
                        break;

                    case VIEW_ACCOUNT_DETAILS:
                        // Access control enforced inside handler via canAccessAccount()
                        this.accountMgr.handleViewAccountDetails();
                        break;

                    case VIEW_ALL_ACCOUNTS:
                        this.accountMgr.handleViewAllAccounts();
                        break;

                    case DELETE_ACCOUNT:
                        this.accountMgr.handleDeleteAccount();
                        break;

                    case UPDATE_OVERDRAFT_LIMIT:
                        this.accountMgr.handleUpdateOverdraftLimit();
                        break;

                    // ===== TRANSACTION OPERATIONS (Both Roles) =====
                    // Account-level access control enforced inside handlers via canAccessAccount()
                    case DEPOSIT_MONEY:
                        this.txProcessor.handleDeposit();
                        break;

                    case WITHDRAW_MONEY:
                        this.txProcessor.handleWithdraw();
                        break;

                    case TRANSFER_MONEY:
                        this.txProcessor.handleTransfer();
                        break;

                    case VIEW_TRANSACTION_HISTORY:
                        this.txProcessor.handleViewTransactionHistory();
                        break;

                    // ===== PROFILE OPERATIONS (Admin Only) =====
                    case CREATE_CUSTOMER_PROFILE:
                        this.customerMgr.handleCreateCustomerProfile();
                        break;

                    case UPDATE_PROFILE_INFORMATION:
                        this.customerMgr.handleUpdateCustomerProfile();
                        break;

                    // ===== REPORTS & UTILITIES (Admin Only) =====
                    case APPLY_INTEREST:
                        this.accountMgr.handleApplyInterest();
                        break;

                    case SORT_ACCOUNTS_BY_NAME:
                        this.accountMgr.handleSortByName();
                        break;

                    case SORT_ACCOUNTS_BY_BALANCE:
                        this.accountMgr.handleSortByBalance();
                        break;

                    case VIEW_AUDIT_TRAIL:
                        // Double-check permission for sensitive operation
                        if (this.hasPermission("VIEW_AUDIT_TRAIL")) {
                            this.displayAuditTrail();
                        } else {
                            System.out.println("✗ You do not have permission to view the audit trail.");
                            if (this.currentUser != null) {
                                this.logAction("VIEW_AUDIT_DENIED", "User attempted to view audit trail without permission");
                            }
                        }
                        break;

                    // ===== SECURITY OPERATIONS (Both Roles) =====
                    case CHANGE_PASSWORD:
                        this.handleChangePassword();
                        break;

                    // ===== SESSION MANAGEMENT (Both Roles) =====
                    case LOGOUT:
                        System.out.println("\n✓ Logging out...");
                        System.out.println("   Please wait while we log you out...\n");
                        this.logout();
                        return MenuAction.LOGOUT;  // Return to login screen

                    case EXIT_APPLICATION:
                        System.out.println("\n✓ Thank you for using the Banking System!");
                        System.out.println("   Goodbye!\n");
                        this.logout();
                        return MenuAction.EXIT_APPLICATION;  // Exit program
                }

            } catch (InputMismatchException e) {
                System.out.println("✗ Invalid input. Please enter a number.");
                this.sc.nextLine(); // clear buffer
            }
        }
        // This should not be reached, but return default action
        return MenuAction.LOGOUT;
    }

    // ===== APPLICATION LIFECYCLE =====

    /**
     * Starts the banking application with multi-user login loop.
     *
     * Handles the complete application lifecycle:
     * - Display login screen
     * - Call runConsoleMenu() for user to login and perform operations
     * - Check MenuAction result to determine next step:
     *   - If LOGOUT: Loop back to login screen (next user can login)
     *   - If EXIT_APPLICATION: Exit loop and terminate application
     *
     * This is the main entry point after demo data initialization.
     */
    public void startApplication() {
        boolean appRunning = true;

        while (appRunning) {
            System.out.println("\n╔════════════════════════════════════╗");
            System.out.println("║      BANKING SYSTEM - LOGIN         ║");
            System.out.println("╚════════════════════════════════════╝");

            // Run console menu (handles login + menu + returns action)
            MenuAction action = runConsoleMenu();

            // Check what user chose
            if (action == MenuAction.EXIT_APPLICATION) {
                appRunning = false;  // Exit the login loop
            }
            // If action is LOGOUT, loop continues and user sees login screen again
        }

        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║        Thank you. Goodbye!         ║");
        System.out.println("╚════════════════════════════════════╝\n");
    }

    // ===== MENU HELPER: ROLE-BASED ACCESS FILTERING =====
    // REMOVED: getPermissionForOption() and shouldShowMenuOption() methods
    //
    // These methods were part of an abandoned redundant permission-checking approach.
    // They checked permissions in the switch statement even though role-based menus
    // (displayAdminMenu vs displayCustomerMenu) already filter options by role.
    //
    // Access control is now enforced at two proper layers:
    // 1. Menu Display Layer: Only show options user has access to
    // 2. Handler Layer: Check canAccessAccount() for shared operations (like viewing accounts)
    //
    // See docs/technical/CODE_FIXES_SUMMARY.md for detailed explanation.

    // ===== AUTHENTICATION FLOW =====

    /**
     * Initiates the login process.
     * Prompts user for username/password with 3-attempt limit.
     *
     * @return the logged-in User, or null if login failed
     */
    public User login() {
        this.currentUser = this.authManager.login(this.sc);
        return this.currentUser;
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        this.authManager.logout();
        this.currentUser = null;
    }

    /**
     * Registers a new user in the authentication system.
     * Used during demo data setup to create admin and customer accounts.
     *
     * @param user the user to register
     * @return true if registration successful
     */
    public boolean registerUser(User user) {
        return this.authManager.registerUser(user);
    }

    /**
     * Returns the currently logged-in user.
     */
    public User getCurrentUser() {
        return this.currentUser;
    }

    /**
     * Returns the authentication manager (for username/password generation).
     * Used by customer and account managers for auto-generation.
     */
    public AuthenticationManager getAuthenticationManager() {
        return this.authManager;
    }

    // ===== AUTHORIZATION =====

    /**
     * Checks if current user has a specific permission.
     */
    public boolean hasPermission(String permission) {
        return this.authManager.hasPermission(permission);
    }

    /**
     * Checks if the current user can access a specific account.
     * Implements role-based access control:
     * - ADMIN users can access ANY account
     * - CUSTOMER users can only access their own linked account
     *
     * This is the centralized access control method - all access checks should use this.
     * Prevents customers from viewing/modifying other customers' accounts.
     *
     * Method calls:
     * - Uses AccountUtils.findAccount() to locate the account
     * - Calls Account.getOwner() to get account owner
     * - Calls Customer.getCustomerId() to compare ownership
     *
     * @param accountNo the account number to check access for
     * @return true if current user can access this account, false otherwise
     */
    public boolean canAccessAccount(String accountNo) {
        if (this.currentUser == null) {
            return false;
        }

        User user = this.currentUser;

        // Admins can access any account
        if (user.getUserRole() == UserRole.ADMIN) {
            return true;
        }

        // Customers can access accounts owned by their linked customer
        if (user instanceof UserAccount) {
            UserAccount customerAccount = (UserAccount) user;
            String linkedCustomerId = customerAccount.getLinkedCustomerId();

            // Find the account and check if its owner matches the linked customer ID
            Account account = AccountUtils.findAccount(this.accountList, accountNo);
            if (account != null && account.getOwner() != null) {
                return account.getOwner().getCustomerId().equals(linkedCustomerId);
            }
            return false;
        }

        return false;
    }

    // ===== BUSINESS OPERATIONS (DELEGATED) =====
    // These methods delegate to the appropriate manager objects.
    // This allows Main.java to call simple methods without needing to access managers directly.

    /**
     * Creates a new customer (delegates to CustomerManager).
     */
    public Customer createCustomer(String customerId, String name) {
        return this.customerMgr.createCustomer(customerId, name);
    }

    /**
     * Creates a new account (delegates to AccountManager).
     */
    public Account createAccount(String customerId, String accountType, String accountNo) {
        return this.accountMgr.createAccount(customerId, accountType, accountNo);
    }

    /**
     * Deposits money into an account (delegates to TransactionProcessor).
     */
    public boolean deposit(String accountNo, double amount) {
        return this.txProcessor.deposit(accountNo, amount);
    }

    /**
     * Withdraws money from an account (delegates to TransactionProcessor).
     */
    public boolean withdraw(String accountNo, double amount) {
        return this.txProcessor.withdraw(accountNo, amount);
    }

    /**
     * Transfers money between two accounts (delegates to TransactionProcessor).
     */
    public boolean transfer(String fromAccountNo, String toAccountNo, double amount) {
        return this.txProcessor.transfer(fromAccountNo, toAccountNo, amount);
    }

    // ===== AUDIT & ADMIN UTILITIES =====

    /**
     * Logs an action to the audit trail.
     */
    public void logAction(String action, String details) {
        if (currentUser != null) {
            this.authManager.logAction(currentUser.getUsername(), currentUser.getUserRole(), action, details);
        }
    }

    /**
     * Handler: Change password for current user.
     * Customers must change their auto-generated password on first login.
     * Uses AuthenticationManager to create new User object (immutable pattern).
     *
     * Method calls:
     * - Calls User.getUsername() to get current username
     * - Calls InputValidator.getCancellableInput() for password prompts (twice)
     * - Calls AuthenticationManager.changePassword() to create new User object
     * - Calls User.setPasswordChangeRequired() to mark password as changed
     */
    private void handleChangePassword() {
        System.out.println("\n--- CHANGE PASSWORD ---");

        // Get current user
        if (this.currentUser == null) {
            System.out.println("✗ No user logged in");
            return;
        }

        String username = this.currentUser.getUsername();

        while (true) {  // OUTER RETRY LOOP
            // Step 1: Prompt for current password
            // Calls InputValidator.getCancellableInput() from this.validator
            String oldPassword = this.validator.getCancellableInput("Enter current password");
            if (oldPassword == null) return;  // User cancelled - exit immediately

            // Step 2: Prompt for new password
            // Calls InputValidator.getCancellableInput() from this.validator
            String newPassword = this.validator.getCancellableInput("Enter new password");
            if (newPassword == null) return;  // User cancelled - exit immediately

            // Step 3: Call AuthenticationManager to change password (creates new User object)
            // Calls AuthenticationManager.changePassword() - immutable pattern
            User newUser = this.authManager.changePassword(username, oldPassword, newPassword);

            if (newUser == null) {
                System.out.println("✗ Password change failed.");

                if (!this.validator.confirmAction("Try again?")) {
                    return;  // User chose no - exit
                }
                // User chose yes - retry from top
            } else {
                // Step 4: Update currentUser reference to new User object
                this.currentUser = newUser;

                // Step 5: Set passwordChangeRequired to false (password has been changed)
                // Calls User.setPasswordChangeRequired() on new user object
                this.currentUser.setPasswordChangeRequired(false);

                // Step 6: Success message
                System.out.println("\n✓ Password changed successfully!");
                System.out.println("  • You are still logged in with your new password");
                System.out.println("  • New password will be used for all future logins\n");
                break;  // Success - exit loop
            }
        }
    }

    /**
     * Displays the audit trail (admin only).
     */
    public void displayAuditTrail() {
        this.authManager.displayAuditTrail();
    }

}
