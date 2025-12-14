package com.banking.managers;

import com.banking.auth.User;
import com.banking.auth.Admin;
import com.banking.auth.UserAccount;
import com.banking.auth.UserRole;
import com.banking.auth.AuditLog;
import com.banking.utilities.InputValidator;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Scanner;
import java.util.Random;

public class AuthenticationManager {
    private LinkedList<User> userRegistry;  // All system users
    private Stack<AuditLog> auditTrail;  // All system operations logged (LIFO - most recent first)
    private User currentUser;  // Currently logged-in user
    private int loginAttempts;  // Track failed login attempts
    private InputValidator validator;  // Input validation and cancellable prompts

    public AuthenticationManager(InputValidator validator) {
        this.userRegistry = new LinkedList<>();
        this.auditTrail = new Stack<>();
        this.currentUser = null;
        this.loginAttempts = 0;
        this.validator = validator;
    }


    public User getCurrentUser() {
        return this.currentUser;
    }


    public boolean registerUser(User user) {
        if (user == null) return false;

        // Check if username already exists
        for (User existing : userRegistry) {
            if (existing.getUsername().equals(user.getUsername())) {
                return false;
            }
        }

        userRegistry.add(user);
        // Log with the new user's username, include who registered them
        String registeredBy = (currentUser != null) ? currentUser.getUsername() : "SYSTEM";
        logAction(user.getUsername(), user.getUserRole(), "USER_REGISTERED", "Registered by: " + registeredBy);
        return true;
    }


    public User login(Scanner sc) {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║   BANKING SYSTEM - LOGIN REQUIRED   ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        this.loginAttempts = 0;
        int maxAttempts = 3;

        while (loginAttempts < maxAttempts) {
            System.out.print("→ Username: ");
            String username = sc.nextLine().trim();

            System.out.print("→ Password: ");
            String password = sc.nextLine().trim();

            // Try to authenticate (checks username AND password together)
            boolean authenticated = false;
            User foundUser = null;

            for (User user : userRegistry) {
                if (user.getUsername().equals(username) && user.authenticate(password)) {
                    authenticated = true;
                    foundUser = user;
                    break;
                }
            }

            // Check result and handle accordingly
            if (authenticated) {
                this.currentUser = foundUser;
                System.out.println("✓ Login successful! Welcome, " + username + "\n");
                logAction(username, foundUser.getUserRole(), "LOGIN_SUCCESS", "User logged in successfully");
                return foundUser;
            } else {
                // Generic error message for security (doesn't reveal if username or password was wrong)
                loginAttempts++;
                System.out.println("✗ Invalid credentials. Attempt " + loginAttempts + "/" + maxAttempts);
                // Log failed attempt with the attempted username (role unknown for security)
                logAction(null, null, "LOGIN_FAILED", "Failed attempt " + loginAttempts + "/" + maxAttempts);
            }
        }

        System.out.println("\n✗ LOGIN FAILED: Maximum attempts exceeded. Exiting...\n");
        return null;
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("✓ " + currentUser.getUsername() + " logged out successfully.");
            logAction(currentUser.getUsername(), currentUser.getUserRole(), "LOGOUT", "User logged out");
            currentUser = null;
        }
    }


    public boolean hasPermission(String permission) {
        if (currentUser == null) return false;
        return currentUser.hasPermission(permission);
    }


    public void logAction(String username, UserRole userRole, String action, String details) {
        // Only log if we have valid action
        if (action == null) return;

        // If no username provided, use current user if available
        if (username == null && currentUser != null) {
            username = currentUser.getUsername();
            if (userRole == null) {
                userRole = currentUser.getUserRole();
            }
        }

        // For security events (LOGIN_FAILED), we may not know the role
        // Use ADMIN as placeholder for unknown role in security events
        if (username != null && userRole == null && action.equals("LOGIN_FAILED")) {
            userRole = UserRole.ADMIN;  // Placeholder for security logs
        }

        // Only add to audit trail if we have username and role
        if (username != null && userRole != null) {
            AuditLog log = new AuditLog(username, userRole, action, details);
            auditTrail.push(log);  // Push to stack (LIFO)
        }
    }


    public String generateUsername(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return null;
        }

        // Convert to lowercase and replace spaces with underscores
        String username = fullName.toLowerCase().trim().replaceAll("\\s+", "_");
        String original = username;

        // Ensure uniqueness by adding counter if needed
        int counter = 1;
        while (true) {
            // Check if username already exists using a traditional for loop
            boolean usernameExists = false;
            for (User user : this.userRegistry) {
                if (user.getUsername().equals(username)) {
                    usernameExists = true;
                    break;
                }
            }

            if (usernameExists) {
                username = original + counter++;
            } else {
                break;  // Username is unique
            }
        }

        return username;
    }

    public String generateTemporaryPassword(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }

        // Get first 2 characters of username (or full username if shorter)
        String firstTwo = username.length() >= 2 ? username.substring(0, 2) : username;

        // Generate random 4-digit number (1000-9999)
        Random random = new Random();
        int randomNum = 1000 + random.nextInt(9000);

        return firstTwo + randomNum;
    }


    public User changePassword(String username, String oldPassword, String newPassword) {

        // Step 2: Find the user
        User currentUser = null;
        int userIndex = -1;
        for (int i = 0; i < this.userRegistry.size(); i++) {
            if (this.userRegistry.get(i).getUsername().equals(username)) {
                currentUser = this.userRegistry.get(i);
                userIndex = i;
                break;
            }
        }

        if (currentUser == null) {
            System.out.println("✗ User not found: " + username);
            return null;
        }

        // Step 3: Verify old password matches
        if (!currentUser.authenticate(oldPassword)) {
            System.out.println("✗ Current password is incorrect");
            return null;
        }

        // Step 4: Validate new password
        if (newPassword.isEmpty()) {
            System.out.println("✗ New password cannot be empty");
            return null;
        }

        if (newPassword.length() < 4) {
            System.out.println("✗ New password must be at least 4 characters");
            return null;
        }

        if (oldPassword.equals(newPassword)) {
            System.out.println("✗ New password must be different from current password");
            return null;
        }

        // Step 5: Create new User object with new password (immutable pattern)
        User newUser = null;
        try {
            if (currentUser instanceof Admin) {
                // Create new Admin with new password
                newUser = new Admin(username, newPassword);
                newUser.setPasswordChangeRequired(false);  // Password has been changed
            } else if (currentUser instanceof UserAccount) {
                // Create new UserAccount with new password, preserving linked customer ID
                UserAccount userAccount = (UserAccount) currentUser;
                newUser = new UserAccount(username, newPassword, userAccount.getLinkedCustomerId());
                newUser.setPasswordChangeRequired(false);  // Password has been changed


            } else {
                System.out.println("✗ Unknown user type");
                return null;
            }
        } catch (Exception e) {
            System.out.println("✗ Error creating new user object: " + e.getMessage());
            return null;
        }

        // Step 6: Replace old user with new user in registry (LinkedList replacement)
        try {
            this.userRegistry.set(userIndex, newUser);
        } catch (Exception e) {
            System.out.println("✗ Error updating user registry: " + e.getMessage());
            return null;
        }

        // Step 7: Log the password change to audit trail
        logAction(username, currentUser.getUserRole(), "CHANGE_PASSWORD", "User successfully changed their password");

        return newUser;  // Return new User object for caller to use as currentUser
    }

    /**
     * Handler: Change password for current user.
     * Customers must change their auto-generated password on first login.
     * Uses changePassword() to create new User object (immutable pattern).
     *
     * Method calls:
     * - Calls getCurrentUser() to get current user
     * - Calls InputValidator.getCancellableInput() for password prompts (twice)
     * - Calls changePassword() to create new User object
     * - Updates user in userRegistry
     *
     * @return Updated User object if password change successful, null otherwise
     */
    public User handleChangePassword() {
        System.out.println("\n--- CHANGE PASSWORD ---");

        String username = this.currentUser.getUsername();

        while (true) {  // OUTER RETRY LOOP
            // Step 1: Prompt for current password
            String oldPassword = this.validator.getCancellableInput("Enter current password");
            if (oldPassword == null) return null;  // User cancelled - exit immediately

            // Step 2: Prompt for new password
            String newPassword = this.validator.getCancellableInput("Enter new password");
            if (newPassword == null) return null;  // User cancelled - exit immediately

            // Step 3: Call changePassword() to validate and create new User object
            User newUser = this.changePassword(username, oldPassword, newPassword);

            if (newUser == null) {
                System.out.println("✗ Password change failed.");

                if (!this.validator.confirmAction("Try again?")) {
                    return null;  // User chose no - exit
                }
                // User chose yes - retry from top
            } else {
                // Step 4: Update currentUser reference to new User object
                this.currentUser = newUser;

                // Step 5: Success message
                System.out.println("\n✓ Password changed successfully!");
                System.out.println("  • You are still logged in with your new password");
                System.out.println("  • New password will be used for all future logins\n");

                return newUser;  // Return new User object for caller to update their reference
            }
        }
    }

    public boolean deleteUserByCustomerId(String customerId) {
        // Find UserAccount linked to this customer ID and delete it
        for (User user : userRegistry) {
            if (user instanceof UserAccount) {
                UserAccount userAccount = (UserAccount) user;
                if (userAccount.getLinkedCustomerId().equals(customerId)) {
                    userRegistry.remove(user);
                    logAction(user.getUsername(), user.getUserRole(), "USER_DELETED", "User account deleted (customer " + customerId + " removed)");
                    return true;
                }
            }
        }
        return false;
    }

    public void displayAuditTrail() {
        if (auditTrail.isEmpty()) {
            System.out.println("No audit logs available.");
            return;
        }

        System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    SYSTEM AUDIT TRAIL (Most Recent First)                 ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝\n");

        // Clone the stack to preserve original data (Stack extends Vector, so clone() works)
        Stack<AuditLog> tempStack = (Stack<AuditLog>) auditTrail.clone();

        // Pop from stack to display most recent first (LIFO behavior)
        while (!tempStack.isEmpty()) {
            System.out.println(tempStack.pop().toString());
        }

        System.out.println("\nTotal operations logged: " + auditTrail.size() + "\n");
    }
}
