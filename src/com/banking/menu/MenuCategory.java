package com.banking.menu;

import com.banking.auth.UserRole;

/**
 * MenuCategory Enum - Categorizes menu actions for better organization
 *
 * This enum encapsulates menu category metadata including:
 * - Display name for UI
 * - Display order for sorting
 * - Role-based availability
 *
 * OOP Principles Demonstrated:
 * - ENCAPSULATION: Category metadata bundled together
 * - TYPE SAFETY: Compile-time validation of categories
 * - SINGLE RESPONSIBILITY: Only manages category definitions
 *
 * Used by: MenuBuilder for auto-generating organized menus
 */
public enum MenuCategory {
    CUSTOMER_OPERATIONS("CUSTOMER OPERATIONS", 1, UserRole.ADMIN),
    ACCOUNT_OPERATIONS("ACCOUNT OPERATIONS", 2, null),
    TRANSACTION_OPERATIONS("TRANSACTION OPERATIONS", 3, null),
    PROFILE_OPERATIONS("PROFILE OPERATIONS", 4, UserRole.ADMIN),
    REPORTS_UTILITIES("REPORTS & UTILITIES", 5, UserRole.ADMIN),
    SECURITY_OPERATIONS("SECURITY OPERATIONS", 6, null),
    SESSION_MANAGEMENT("SESSION MANAGEMENT", 7, null);

    private final String displayName;
    private final int displayOrder;
    private final UserRole requiredRole;  // null = available to all roles

    /**
     * Constructor for MenuCategory enum values.
     *
     * @param displayName The display name shown in menus
     * @param displayOrder The order in which categories are displayed (1-7)
     * @param requiredRole The role required to see this category (null = all roles)
     */
    MenuCategory(String displayName, int displayOrder, UserRole requiredRole) {
        this.displayName = displayName;
        this.displayOrder = displayOrder;
        this.requiredRole = requiredRole;
    }

    /**
     * Gets the display name for this category.
     *
     * @return Display name string
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the display order for sorting categories.
     *
     * @return Display order integer (1-7)
     */
    public int getDisplayOrder() {
        return displayOrder;
    }

    /**
     * Checks if this category is available for the given user role.
     *
     * @param role The user role to check
     * @return true if category is available for this role
     */
    public boolean isAvailableFor(UserRole role) {
        if (requiredRole == null) {
            return true;  // Available to all roles
        }
        return requiredRole == role;  // Must match required role
    }
}
