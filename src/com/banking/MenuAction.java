package com.banking;

import com.banking.auth.UserRole;
import com.banking.menu.MenuCategory;

/**
 * MenuAction Enum - Represents ALL menu options with dual numbering system
 *
 * This enum provides a sophisticated solution for role-based menus:
 * - Admin users see menu numbers 1-21 (unchanged from original)
 * - Customer users see menu numbers 1-7, 0 (clean sequential numbering)
 * - Each action has BOTH admin and customer numbers
 * - Role-aware lookup automatically maps user input to correct action
 *
 * OOP Principles Demonstrated:
 * - ENCAPSULATION: All menu metadata in one place
 * - ABSTRACTION: Complex numbering logic hidden behind simple methods
 * - TYPE SAFETY: Enum provides compile-time checking (no magic numbers)
 * - POLYMORPHISM: Same enum behaves differently based on role
 *
 * Data Structures:
 * - Enum with multiple fields (composition)
 * - Lookup/mapping via fromMenuNumber()
 * - Filtering via isAvailableFor()
 */
public enum MenuAction {
    // ===== CUSTOMER OPERATIONS (Admin Only) =====
    // adminNum, customerNum (-1 = not available to customers)
    CREATE_CUSTOMER("Create Customer", 1, -1, UserRole.ADMIN, MenuCategory.CUSTOMER_OPERATIONS),
    VIEW_CUSTOMER_DETAILS("View Customer Details", 2, -1, UserRole.ADMIN, MenuCategory.CUSTOMER_OPERATIONS),
    VIEW_ALL_CUSTOMERS("View All Customers", 3, -1, UserRole.ADMIN, MenuCategory.CUSTOMER_OPERATIONS),
    DELETE_CUSTOMER("Delete Customer", 4, -1, UserRole.ADMIN, MenuCategory.CUSTOMER_OPERATIONS),

    // ===== ACCOUNT OPERATIONS =====
    CREATE_ACCOUNT("Create Account", 5, -1, UserRole.ADMIN, MenuCategory.ACCOUNT_OPERATIONS),
    VIEW_ACCOUNT_DETAILS("View Account Details", 6, 1, null, MenuCategory.ACCOUNT_OPERATIONS),  // Both roles - customer sees as #1
    VIEW_ALL_ACCOUNTS("View All Accounts", 7, -1, UserRole.ADMIN, MenuCategory.ACCOUNT_OPERATIONS),
    DELETE_ACCOUNT("Delete Account", 8, -1, UserRole.ADMIN, MenuCategory.ACCOUNT_OPERATIONS),
    UPDATE_OVERDRAFT_LIMIT("Update Overdraft Limit (Checking)", 9, -1, UserRole.ADMIN, MenuCategory.ACCOUNT_OPERATIONS),

    // ===== TRANSACTION OPERATIONS (Both Roles) =====
    DEPOSIT_MONEY("Deposit Money", 10, 2, null, MenuCategory.TRANSACTION_OPERATIONS),           // Both roles - customer sees as #2
    WITHDRAW_MONEY("Withdraw Money", 11, 3, null, MenuCategory.TRANSACTION_OPERATIONS),         // Both roles - customer sees as #3
    TRANSFER_MONEY("Transfer Money", 12, 4, null, MenuCategory.TRANSACTION_OPERATIONS),         // Both roles - customer sees as #4
    VIEW_TRANSACTION_HISTORY("View Transaction History", 13, 5, null, MenuCategory.TRANSACTION_OPERATIONS),  // Both roles - customer sees as #5

    // ===== PROFILE OPERATIONS (Admin Only) =====
    CREATE_CUSTOMER_PROFILE("Create/Update Customer Profile", 14, -1, UserRole.ADMIN, MenuCategory.PROFILE_OPERATIONS),
    UPDATE_PROFILE_INFORMATION("Update Profile Information", 15, -1, UserRole.ADMIN, MenuCategory.PROFILE_OPERATIONS),

    // ===== REPORTS & UTILITIES (Admin Only) =====
    APPLY_INTEREST("Apply Interest (All Savings Accounts)", 16, -1, UserRole.ADMIN, MenuCategory.REPORTS_UTILITIES),
    SORT_ACCOUNTS_BY_NAME("Sort Accounts by Name", 17, -1, UserRole.ADMIN, MenuCategory.REPORTS_UTILITIES),
    SORT_ACCOUNTS_BY_BALANCE("Sort Accounts by Balance", 18, -1, UserRole.ADMIN, MenuCategory.REPORTS_UTILITIES),
    VIEW_AUDIT_TRAIL("View Audit Trail (Admin Only)", 19, -1, UserRole.ADMIN, MenuCategory.REPORTS_UTILITIES),

    // ===== SESSION MANAGEMENT (Both Roles) =====
    EXIT_APPLICATION("Exit Application", 20, 7, null, MenuCategory.SESSION_MANAGEMENT),     // Both roles - customer sees as #7
    CHANGE_PASSWORD("Change Password", 21, 6, null, MenuCategory.SECURITY_OPERATIONS),       // Both roles - customer sees as #6

    // ===== SPECIAL ACTIONS =====
    LOGOUT("Logout (Return to Login)", 0, 0, null, MenuCategory.SESSION_MANAGEMENT);        // Both roles - both see as #0

    // ===== ENUM FIELDS =====
    private final String displayName;
    private final int adminMenuNumber;       // Number shown to admin users
    private final int customerMenuNumber;    // Number shown to customer users (-1 = not available)
    private final UserRole requiredRole;     // Required role (null = both roles can access)
    private final MenuCategory category;     // Menu category for organization

    /**
     * Constructor for MenuAction enum values.
     *
     * @param displayName Human-readable name shown in menu
     * @param adminNumber Menu number for admin users
     * @param customerNumber Menu number for customer users (-1 = not available)
     * @param requiredRole Required role (ADMIN, CUSTOMER, or null for both)
     * @param category Menu category for organization
     */
    MenuAction(String displayName, int adminNumber, int customerNumber, UserRole requiredRole, MenuCategory category) {
        this.displayName = displayName;
        this.adminMenuNumber = adminNumber;
        this.customerMenuNumber = customerNumber;
        this.requiredRole = requiredRole;
        this.category = category;
    }

    // ===== GETTERS =====

    /**
     * Gets the display name for this menu action.
     * @return Display name (e.g., "Create Customer")
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the role-appropriate menu number for this action.
     * This is the KEY method that enables dual numbering!
     *
     * Example:
     *   DEPOSIT_MONEY.getMenuNumber(UserRole.ADMIN) → 10
     *   DEPOSIT_MONEY.getMenuNumber(UserRole.CUSTOMER) → 2
     *
     * @param role The user's role
     * @return Menu number appropriate for this role
     */
    public int getMenuNumber(UserRole role) {
        if (role == UserRole.CUSTOMER) {
            return customerMenuNumber;
        }
        return adminMenuNumber;
    }

    /**
     * Gets the menu category for this action.
     * @return MenuCategory enum value
     */
    public MenuCategory getCategory() {
        return category;
    }

    // ===== PERMISSION CHECKING =====

    /**
     * Checks if a given user role can access this menu action.
     *
     * Permission logic:
     * - If requiredRole is null → Available to all roles
     * - If requiredRole is ADMIN → Only ADMIN can access
     * - If requiredRole is CUSTOMER → Only CUSTOMER can access
     *
     * @param userRole The user's role to check
     * @return true if user role can access this action, false otherwise
     */
    public boolean canAccess(UserRole userRole) {
        if (userRole == null) {
            return false;  // No role = no access
        }
        if (this.requiredRole == null) {
            return true;  // Available to all roles
        }
        return this.requiredRole == userRole;  // Must match required role
    }

    /**
     * Checks if this action should be shown in the menu for a given role.
     * Determines menu display filtering.
     *
     * Logic:
     * - For CUSTOMER: Only show if customerMenuNumber is valid (>= 0)
     * - For ADMIN: Show everything
     *
     * @param userRole The user's role
     * @return true if this action should appear in the menu for this role
     */
    public boolean isAvailableFor(UserRole userRole) {
        if (userRole == UserRole.CUSTOMER) {
            // Customer can only see actions with valid customer numbers
            return customerMenuNumber >= 0;  // 0 is valid (for LOGOUT)
        }
        return true;  // Admins see all actions
    }

    // ===== STATIC CONVERSION METHOD =====

    /**
     * Converts a menu number to a MenuAction enum value (ROLE-AWARE).
     * This is the heart of the dual numbering system!
     *
     * Example:
     *   fromMenuNumber(2, UserRole.ADMIN) → VIEW_CUSTOMER_DETAILS
     *   fromMenuNumber(2, UserRole.CUSTOMER) → DEPOSIT_MONEY
     *
     * The same input number (2) maps to DIFFERENT actions based on role!
     *
     * @param menuNumber The menu number entered by user
     * @param role The user's role (determines which number set to use)
     * @return Corresponding MenuAction, or null if not found
     */
    public static MenuAction fromMenuNumber(int menuNumber, UserRole role) {
        for (MenuAction action : MenuAction.values()) {
            if (action.getMenuNumber(role) == menuNumber) {
                return action;
            }
        }
        return null;  // Invalid menu number for this role
    }

    // ===== HELPER METHODS =====

    @Override
    public String toString() {
        String roleStr = (requiredRole == null) ? "ALL" : requiredRole.getDisplayName();
        return String.format("[Admin:%d Customer:%d] %s (Role: %s)",
                adminMenuNumber, customerMenuNumber, displayName, roleStr);
    }
}
