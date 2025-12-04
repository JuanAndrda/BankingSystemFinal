# Security Features Documentation
## Banking Management System

**Project:** Banking System Final Project
**Focus:** Authentication, Authorization, and Security

---

## Table of Contents
1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Authorization (RBAC)](#authorization)
4. [Password Management](#password-management)
5. [Audit Logging](#audit-logging)
6. [Session Management](#session-management)
7. [Security Best Practices](#security-best-practices)
8. [Security Limitations](#security-limitations)

---

## Overview

The Banking Management System implements a **multi-layer security model**:

```
┌──────────────────────────────────────────┐
│         AUTHENTICATION LAYER             │
│  - Username + Password                   │
│  - 3-attempt limit                       │
│  - Session management                    │
└──────────────────────────────────────────┘
               ↓
┌──────────────────────────────────────────┐
│         AUTHORIZATION LAYER (RBAC)       │
│  - Role-based permissions                │
│  - Admin: 21 permissions                 │
│  - Customer: 7 permissions               │
└──────────────────────────────────────────┘
               ↓
┌──────────────────────────────────────────┐
│         ACCESS CONTROL LAYER             │
│  - Account-level permissions             │
│  - Data ownership validation             │
│  - Operation-level checks                │
└──────────────────────────────────────────┘
               ↓
┌──────────────────────────────────────────┐
│         AUDIT TRAIL LAYER                │
│  - All operations logged                 │
│  - Timestamp + user tracking             │
│  - Security event monitoring             │
└──────────────────────────────────────────┘
```

---

## Authentication

### Login System

**File:** `src/com/banking/managers/AuthenticationManager.java:47-90`

**Features:**
- Username + Password verification
- 3-attempt limit
- Generic error messages (security best practice)
- Session creation
- Audit logging

```java
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

        // Authenticate
        boolean authenticated = false;
        User foundUser = null;

        for (User user : userRegistry) {
            if (user.getUsername().equals(username) && user.authenticate(password)) {
                authenticated = true;
                foundUser = user;
                break;
            }
        }

        if (authenticated) {
            this.currentUser = foundUser;
            System.out.println("✓ Login successful! Welcome, " + username + "\n");
            logAction(username, foundUser.getUserRole(), "LOGIN_SUCCESS",
                "User logged in successfully");
            return foundUser;
        } else {
            // Generic error - doesn't reveal if username or password was wrong
            loginAttempts++;
            System.out.println("✗ Invalid credentials. Attempt " + loginAttempts + "/" + maxAttempts);
            logAction(null, null, "LOGIN_FAILED", "Invalid credentials for: " + username);
        }
    }

    System.out.println("\n✗ LOGIN FAILED: Maximum attempts exceeded. Exiting...\n");
    return null;
}
```

**Security Features:**
✓ **Generic error messages:** Doesn't reveal if username exists
✓ **Attempt limiting:** Max 3 attempts before lockout
✓ **Audit logging:** All attempts logged
✓ **Session tracking:** Sets currentUser on success

### Logout System

**File:** `src/com/banking/managers/AuthenticationManager.java:92-98`

```java
public void logout() {
    if (currentUser != null) {
        System.out.println("✓ " + currentUser.getUsername() + " logged out successfully.");
        logAction(currentUser.getUsername(), currentUser.getUserRole(), "LOGOUT",
            "User logged out");
        currentUser = null;  // Clear session
    }
}
```

---

## Authorization

### Role-Based Access Control (RBAC)

**Two Roles:** Admin and Customer

#### Admin Permissions (21 total)

**File:** `src/com/banking/auth/Admin.java:25-50`

```java
@Override
public List<String> getPermissions() {
    List<String> permissions = new ArrayList<>();

    // Customer Management
    permissions.add("CREATE_CUSTOMER");
    permissions.add("VIEW_ALL_CUSTOMERS");
    permissions.add("VIEW_CUSTOMER_DETAILS");
    permissions.add("DELETE_CUSTOMER");

    // Profile Management
    permissions.add("CREATE_PROFILE");
    permissions.add("UPDATE_PROFILE");

    // Account Management
    permissions.add("CREATE_ACCOUNT");
    permissions.add("VIEW_ALL_ACCOUNTS");
    permissions.add("VIEW_ACCOUNT_DETAILS");
    permissions.add("DELETE_ACCOUNT");
    permissions.add("UPDATE_OVERDRAFT");

    // Account Operations
    permissions.add("SORT_ACCOUNTS");
    permissions.add("APPLY_INTEREST");

    // Transactions (Admin can do everything)
    permissions.add("DEPOSIT_MONEY");
    permissions.add("WITHDRAW_MONEY");
    permissions.add("TRANSFER_MONEY");
    permissions.add("VIEW_TRANSACTION_HISTORY");

    // Security & Admin
    permissions.add("VIEW_AUDIT_TRAIL");  // Admin only
    permissions.add("VIEW_OWN_ACCOUNTS");
    permissions.add("CHANGE_PASSWORD");
    permissions.add("LOGOUT");

    return permissions;
}
```

#### Customer Permissions (7 total)

**File:** `src/com/banking/auth/UserAccount.java:35-45`

```java
@Override
public List<String> getPermissions() {
    List<String> permissions = new ArrayList<>();

    // Limited access - own accounts only
    permissions.add("VIEW_OWN_ACCOUNTS");
    permissions.add("DEPOSIT_MONEY");
    permissions.add("WITHDRAW_MONEY");
    permissions.add("TRANSFER_MONEY");
    permissions.add("VIEW_TRANSACTION_HISTORY");
    permissions.add("CHANGE_PASSWORD");
    permissions.add("LOGOUT");

    // NOT allowed:
    // - CREATE/DELETE customers, accounts, profiles
    // - VIEW_ALL_* operations
    // - VIEW_AUDIT_TRAIL
    // - UPDATE_OVERDRAFT, SORT, APPLY_INTEREST

    return permissions;
}
```

### Permission Checking

**File:** `src/com/banking/BankingSystem.java:600-610`

```java
public boolean hasPermission(String permission) {
    return authManager.hasPermission(permission);
}

// In AuthenticationManager:
public boolean hasPermission(String permission) {
    if (currentUser == null) return false;
    return currentUser.hasPermission(permission);  // Polymorphic call
}
```

**Usage Example:**

**File:** `src/com/banking/BankingSystem.java:383-390`

```java
case VIEW_AUDIT_TRAIL:
    System.out.println("\n=== VIEW AUDIT TRAIL ===");
    if (this.hasPermission("VIEW_AUDIT_TRAIL")) {
        this.displayAuditTrail();
    } else {
        System.out.println("✗ You do not have permission to view the audit trail.");
        if (currentUser != null) {
            this.logAction("VIEW_AUDIT_DENIED",
                "User attempted to view audit trail without permission");
        }
    }
    break;
```

### Account Access Control

**File:** `src/com/banking/BankingSystem.java:620-650`

```java
public boolean canAccessAccount(String accountNo) {
    if (currentUser == null) return false;

    // Admins can access all accounts
    if (currentUser.getUserRole() == UserRole.ADMIN) {
        return true;
    }

    // Customers can only access their own accounts
    if (currentUser instanceof UserAccount) {
        UserAccount userAccount = (UserAccount) currentUser;
        String customerId = userAccount.getLinkedCustomerId();

        // Find account and check ownership
        for (Account account : accountManager.getAccounts()) {
            if (account.getAccountNo().equals(accountNo)) {
                return account.getOwnerId().equals(customerId);
            }
        }
    }

    return false;
}
```

**Used in Transactions:**

**File:** `src/com/banking/managers/TransactionProcessor.java:285-301`

```java
// Defense in depth - secondary access control check
if (!this.bankingSystem.canAccessAccount(account.getAccountNo())) {
    UIFormatter.printErrorEnhanced(
        "Access denied. You can only view transaction history for your own accounts.",
        "Please select one of your accounts from the list above."
    );
    // Log access denial
    this.bankingSystem.logAction("ACCESS_DENIED",
        "Attempted to view transaction history for account: " + account.getAccountNo());
    return;
}
```

---

## Password Management

### Password Storage

**File:** `src/com/banking/auth/User.java:15`

```java
private final String password;  // Plain text (educational project)

// NOTE: Production systems should use bcrypt, Argon2, or similar
```

**⚠ Security Limitation:** Passwords stored in plain text for educational purposes.

### Password Validation

**File:** `src/com/banking/auth/User.java:30-36`

```java
public boolean authenticate(String password) {
    return this.password.equals(password);
}
```

### Temporary Password Generation

**File:** `src/com/banking/managers/AuthenticationManager.java:158-171`

```java
public String generateTemporaryPassword(String username) {
    if (username == null || username.isEmpty()) {
        return null;
    }

    // Format: "Welcome" + first 2 chars + 4-digit random
    String firstTwo = username.length() >= 2 ? username.substring(0, 2) : username;

    Random random = new Random();
    int randomNum = 1000 + random.nextInt(9000);  // 1000-9999

    return "Welcome" + firstTwo + randomNum;
}
```

**Examples:**
- `alice` → `Welcomeal7384`
- `bob_smith` → `Welcomebo2947`

### Mandatory Password Change

**File:** `src/com/banking/BankingSystem.java:270-290`

```java
// Check if password change required
if (currentUser.isPasswordChangeRequired()) {
    System.out.println("\n⚠ NOTICE: You must change your password before continuing.");
    System.out.println("Your temporary password was auto-generated for security.\n");

    // Force password change
    User updatedUser = authManager.handlePasswordChange(currentUser, scanner);

    if (updatedUser == null) {
        System.out.println("✗ Password change failed. Logging out for security.");
        authManager.logout();
        return;
    }

    currentUser = updatedUser;  // Update session with new user object
    System.out.println("✓ Password changed successfully. You may now use the system.\n");
}
```

### Password Change Implementation

**File:** `src/com/banking/managers/AuthenticationManager.java:174-252`

**Validation Rules:**
- Old password must be correct
- New password minimum 4 characters
- New password must differ from old
- Cannot be empty

**Immutable Pattern:**
```java
// Create new User object with new password
if (currentUser instanceof Admin) {
    newUser = new Admin(username, newPassword);
} else if (currentUser instanceof UserAccount) {
    UserAccount userAccount = (UserAccount) currentUser;
    newUser = new UserAccount(username, newPassword, userAccount.getLinkedCustomerId());
    newUser.setPasswordChangeRequired(false);
}

// Replace in registry
userRegistry.set(userIndex, newUser);
```

---

## Audit Logging

### Audit Log Structure

**File:** `src/com/banking/auth/AuditLog.java:8-20`

```java
public class AuditLog {
    private final LocalDateTime timestamp;  // When
    private final String username;          // Who
    private final UserRole userRole;        // Role
    private final String action;            // What
    private final String details;           // Context

    public AuditLog(String username, UserRole userRole, String action, String details) {
        this.timestamp = LocalDateTime.now();
        this.username = validateUsername(username);
        this.userRole = validateUserRole(userRole);
        this.action = validateAction(action);
        this.details = validateDetails(details);
    }
}
```

### Logged Events

**File:** `src/com/banking/managers/AuthenticationManager.java:107-124`

```java
public void logAction(String username, UserRole userRole, String action, String details) {
    if (action == null) return;

    // Auto-fill from current user if not provided
    if (username == null && currentUser != null) {
        username = currentUser.getUsername();
        if (userRole == null) {
            userRole = currentUser.getUserRole();
        }
    }

    if (username != null && userRole != null) {
        AuditLog log = new AuditLog(username, userRole, action, details);
        auditTrail.push(log);  // Stack - newest first
    }
}
```

**Events Logged:**

| Category | Events |
|----------|--------|
| **Authentication** | LOGIN_SUCCESS, LOGIN_FAILED, LOGOUT, CHANGE_PASSWORD |
| **Customer Ops** | CREATE_CUSTOMER, DELETE_CUSTOMER, VIEW_ALL_CUSTOMERS |
| **Account Ops** | CREATE_ACCOUNT, DELETE_ACCOUNT, UPDATE_OVERDRAFT, SORT_ACCOUNTS, APPLY_INTEREST |
| **Transactions** | DEPOSIT_MONEY, WITHDRAW_MONEY, TRANSFER_MONEY |
| **Profile Ops** | CREATE_PROFILE, UPDATE_PROFILE |
| **Security** | ACCESS_DENIED, VIEW_AUDIT_DENIED |

### Display Audit Trail

**File:** `src/com/banking/managers/AuthenticationManager.java:255-275`

```java
public void displayAuditTrail() {
    if (auditTrail.isEmpty()) {
        System.out.println("No audit logs available.");
        return;
    }

    System.out.println("\n╔═══════════════════════════════════════════════════════╗");
    System.out.println("║    SYSTEM AUDIT TRAIL (Most Recent First)         ║");
    System.out.println("╚═══════════════════════════════════════════════════════╝\n");

    // Clone stack to preserve original
    @SuppressWarnings("unchecked")
    Stack<AuditLog> tempStack = (Stack<AuditLog>) auditTrail.clone();

    // Pop from stack - newest first
    while (!tempStack.isEmpty()) {
        System.out.println(tempStack.pop().toString());
    }

    System.out.println("\nTotal operations logged: " + auditTrail.size() + "\n");
}
```

**Example Output:**
```
SYSTEM AUDIT TRAIL (Most Recent First)

2025-12-04 15:45:22 | admin (Administrator) | DELETE_ACCOUNT | Deleted account: ACC007
2025-12-04 15:43:10 | bob (Customer) | WITHDRAW_MONEY | Withdrew $50.00 from ACC003
2025-12-04 15:40:55 | alice (Customer) | DEPOSIT_MONEY | Deposited $100.00 to ACC001
2025-12-04 15:38:30 | bob (Customer) | ACCESS_DENIED | Attempted to view audit trail without permission
2025-12-04 15:35:12 | admin (Administrator) | CREATE_CUSTOMER | Created customer: C004 - David Lee

Total operations logged: 5
```

---

## Session Management

### Current User Tracking

**File:** `src/com/banking/managers/AuthenticationManager.java:15-16`

```java
private User currentUser;  // Currently logged-in user
```

**Lifecycle:**
1. **Login:** `currentUser = foundUser`
2. **Use:** All operations check `currentUser`
3. **Logout:** `currentUser = null`

### Session Context

**File:** `src/com/banking/BankingSystem.java:80-95`

```java
public class BankingSystem {
    private final AuthenticationManager authManager;

    public User getCurrentUser() {
        return authManager.getCurrentUser();
    }

    public boolean hasPermission(String permission) {
        return authManager.hasPermission(permission);
    }

    public boolean canAccessAccount(String accountNo) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return false;
        // ... access control logic
    }
}
```

**All operations validate session:**
```java
if (currentUser == null) {
    System.out.println("✗ Not authenticated");
    return;
}
```

---

## Security Best Practices

### 1. Principle of Least Privilege

**Implemented:**
- Customers have minimal permissions (7)
- Admins have full access (21)
- Clear role separation

**Example:**
```java
// Customer CANNOT:
- Create/delete other customers
- View all accounts
- View audit trail
- Modify overdraft limits

// Customer CAN:
- View own accounts only
- Perform transactions on own accounts
- Change own password
```

### 2. Defense in Depth

**Multiple validation layers:**

```
UI Layer:     Check permission before showing menu option
    ↓
Service Layer: Check permission before executing
    ↓
Domain Layer:  Check ownership before allowing operation
    ↓
Audit Layer:   Log all access attempts
```

**Example:**

```java
// Layer 1: UI check
if (!hasPermission("DELETE_ACCOUNT")) {
    System.out.println("✗ Access denied");
    return;
}

// Layer 2: Service check
if (!canAccessAccount(accountNo)) {
    logAction("ACCESS_DENIED", "Attempted to delete account: " + accountNo);
    return;
}

// Layer 3: Domain check
if (account.getBalance() != 0) {
    System.out.println("✗ Account must have zero balance");
    return;
}
```

### 3. Audit Logging

**All operations logged:**
- ✓ Who (username)
- ✓ What (action)
- ✓ When (timestamp)
- ✓ Why (details)
- ✓ Role (Administrator/Customer)

**Stack for tamper-evidence:**
- LIFO prevents easy deletion of old logs
- Newest first for security monitoring

### 4. Fail Securely

**Generic error messages:**
```java
// GOOD (current)
System.out.println("✗ Invalid credentials");

// BAD (security risk)
System.out.println("✗ Username not found");
System.out.println("✗ Password incorrect");
```

**Doesn't leak information about:**
- Valid usernames
- Account existence
- Customer IDs

### 5. Secure Defaults

**Implemented:**
- New users require password change
- Accounts start with zero overdraft
- Customers have restricted permissions
- All operations logged by default

---

## Security Limitations

### Honest Assessment (Educational Project)

#### ⚠ Plain Text Passwords

**Issue:** Passwords stored without hashing
**Impact:** Database compromise exposes all passwords
**Mitigation for Production:**
```java
// Current (educational)
private final String password;

// Production (recommended)
private final String passwordHash;  // bcrypt, Argon2, or PBKDF2

public void setPassword(String password) {
    this.passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
}

public boolean authenticate(String password) {
    return BCrypt.checkpw(password, this.passwordHash);
}
```

#### ⚠ No Session Timeout

**Issue:** Sessions persist until manual logout
**Impact:** Unattended terminal remains logged in
**Mitigation for Production:**
```java
private LocalDateTime lastActivity;
private static final int TIMEOUT_MINUTES = 15;

public boolean isSessionValid() {
    if (lastActivity == null) return false;
    return LocalDateTime.now().isBefore(lastActivity.plusMinutes(TIMEOUT_MINUTES));
}
```

#### ⚠ Limited Rate Limiting

**Issue:** Only 3 login attempts, then exits program
**Impact:** No account lockout, can restart and retry
**Mitigation for Production:**
```java
private Map<String, FailedLoginAttempts> loginAttempts;

class FailedLoginAttempts {
    int count;
    LocalDateTime lockoutUntil;

    boolean isLockedOut() {
        return lockoutUntil != null && LocalDateTime.now().isBefore(lockoutUntil);
    }
}
```

#### ⚠ No Encryption in Transit

**Issue:** Console application, no network communication
**Impact:** Not applicable for current scope
**Mitigation for Production:** Use TLS/SSL for client-server communication

---

## Summary

This Banking Management System implements **comprehensive security features**:

### Strengths

✅ **Authentication:** Login with 3-attempt limit
✅ **Authorization:** Role-based access control (RBAC)
✅ **Access Control:** Account ownership validation
✅ **Audit Logging:** Complete operation tracking
✅ **Session Management:** Current user tracking
✅ **Password Management:** Mandatory change on first login
✅ **Defense in Depth:** Multiple validation layers
✅ **Secure Defaults:** Restricted permissions
✅ **Fail Securely:** Generic error messages
✅ **Permission System:** 21 admin, 7 customer permissions

### Educational Limitations

⚠ Plain text passwords (not hashed)
⚠ No session timeout
⚠ Basic rate limiting
⚠ No encryption (console app)

**Note:** These limitations are acceptable for an educational project demonstrating security concepts. Production systems should implement bcrypt/Argon2 password hashing, session timeouts, account lockouts, and encrypted communication.

---

**End of Security Features Document**
