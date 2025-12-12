package com.banking.auth;

import java.util.LinkedList;

// Customer user account with access to own accounts only
public class UserAccount extends User {
    private final String linkedCustomerId;

    public UserAccount(String username, String password, String linkedCustomerId) {
        super(username, password, UserRole.CUSTOMER, true);
        this.linkedCustomerId = validateLinkedCustomerId(linkedCustomerId);
    }

    // Validate linked customer ID
    private String validateLinkedCustomerId(String linkedCustomerId) {
        if (!com.banking.utilities.ValidationPatterns.matchesPattern(linkedCustomerId,
                com.banking.utilities.ValidationPatterns.CUSTOMER_ID_PATTERN)) {
            throw new IllegalArgumentException(com.banking.utilities.ValidationPatterns.CUSTOMER_ID_ERROR);
        }
        return linkedCustomerId;
    }

    public String getLinkedCustomerId() {
        return this.linkedCustomerId;
    }

    @Override
    public LinkedList<String> getPermissions() {
        LinkedList<String> permissions = new LinkedList<>();

        // Session management
        permissions.add("LOGOUT");
        permissions.add("EXIT_APP");

        // Account operations (own accounts only)
        permissions.add("VIEW_ACCOUNT_DETAILS");

        // Transaction operations (own accounts only)
        permissions.add("DEPOSIT_MONEY");
        permissions.add("WITHDRAW_MONEY");
        permissions.add("TRANSFER_MONEY");
        permissions.add("VIEW_TRANSACTION_HISTORY");

        // Security operations
        permissions.add("CHANGE_PASSWORD");

        return permissions;
    }
}
