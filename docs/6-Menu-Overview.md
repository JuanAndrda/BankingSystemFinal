# Banking System - Menu Documentation Overview

**Project:** Banking Management System Final Project
**Courses:** CIT 207 (Object-Oriented Programming) & CC 204 (Data Structures and Algorithms)
**Document:** Menu System Architecture and Overview
**Date:** December 2025

---

## 📑 Table of Contents

1. [Introduction](#introduction)
2. [Menu Architecture](#menu-architecture)
3. [Dual Numbering System](#dual-numbering-system)
4. [Complete Menu Reference](#complete-menu-reference)
5. [Access Control Summary](#access-control-summary)
6. [Documentation Index](#documentation-index)

---

## Introduction

This document provides a comprehensive overview of the Banking System's menu architecture. The system implements a sophisticated **dual numbering system** that presents different menu layouts based on user roles while using a unified codebase.

### Key Features

- **22 Unique Menu Actions** (including login)
- **Role-Based Menus** (Admin vs Customer)
- **Dual Numbering System** (same action, different numbers per role)
- **7 Menu Categories** (organized by functionality)
- **Enum-Based Architecture** (type-safe, compile-time validation)

### Documentation Organization

The complete menu documentation is split across 7 files for easy navigation:

| File | Content | Lines |
|------|---------|-------|
| **6-Menu-Overview.md** | This file - Architecture and summary tables | ~300 |
| **6.1-Login.md** | Login screen authentication flow | ~400 |
| **6.2-Customer-Ops.md** | Customer Operations (Admin #1-4) | ~1,000 |
| **6.3-Account-Ops.md** | Account Operations (Admin #5-9) | ~1,250 |
| **6.4-Transaction-Ops.md** | Transaction Operations (Admin #10-13, Customer #2-5) | ~1,000 |
| **6.5-Profile-Reports.md** | Profile Operations & Reports (Admin #14-19) | ~1,500 |
| **6.6-Security-Session.md** | Security & Session Management (Admin #21, #0, #20) | ~700 |
| **Total** | | **~6,150** |

---

## Menu Architecture

### Implementation Files

The menu system is built using a modern enum-based architecture:

| File | Purpose | Location |
|------|---------|----------|
| **MenuAction.java** | Defines all 22 menu actions with dual numbering | `src/com/banking/MenuAction.java` |
| **MenuBuilder.java** | Auto-generates role-specific menus from enum | `src/com/banking/menu/MenuBuilder.java` |
| **MenuCategory.java** | Categorizes actions into 7 logical groups | `src/com/banking/menu/MenuCategory.java` |
| **BankingSystem.java** | Menu display and action routing | `src/com/banking/BankingSystem.java:127-412` |

### Menu Categories

All 22 menu actions are organized into 7 categories:

| # | Category | Display Name | Admin Options | Customer Options | Purpose |
|---|----------|--------------|---------------|------------------|---------|
| 1 | CUSTOMER_OPERATIONS | "CUSTOMER OPERATIONS" | 4 | 0 | Customer CRUD (Admin only) |
| 2 | ACCOUNT_OPERATIONS | "ACCOUNT OPERATIONS" | 4 | 1 | Account management |
| 3 | TRANSACTION_OPERATIONS | "TRANSACTION OPERATIONS" | 4 | 4 | Money operations (shared) |
| 4 | PROFILE_OPERATIONS | "PROFILE OPERATIONS" | 2 | 0 | Profile management (Admin only) |
| 5 | REPORTS_UTILITIES | "REPORTS & UTILITIES" | 4 | 0 | Reporting and sorting (Admin only) |
| 6 | SECURITY_OPERATIONS | "SECURITY OPERATIONS" | 1 | 1 | Password management (shared) |
| 7 | SESSION_MANAGEMENT | "SESSION MANAGEMENT" | 2 | 2 | Logout and exit (shared) |

**Total Actions:** 21 admin + 8 customer (including logout) = 22 unique actions

---

## Dual Numbering System

### Overview

The Banking System uses a **dual numbering system** where the same menu action has different numbers depending on the user's role. This provides:

- **Sequential numbering** for each role (no gaps)
- **Role-appropriate menus** (customers don't see admin options)
- **Single source of truth** (one enum definition per action)
- **Type safety** (compile-time validation via enums)

### How It Works

Each `MenuAction` enum has two number fields:

```java
MenuAction(String displayName, int adminNumber, int customerNumber,
           UserRole requiredRole, MenuCategory category)
```

**Example - Deposit Money:**

```java
DEPOSIT_MONEY("Deposit Money", 10, 2, null, MenuCategory.TRANSACTION_OPERATIONS)
//                              ↑   ↑   ↑
//                         Admin  Cust  Both roles
```

- **Admin sees:** Option #10 in their menu
- **Customer sees:** Option #2 in their menu
- **Handler:** Same method (`TransactionProcessor.handleDeposit()`)

### Role-Aware Lookup

The enum provides role-aware conversion:

```java
MenuAction action = MenuAction.fromMenuNumber(2, UserRole.ADMIN);
// Returns: VIEW_CUSTOMER_DETAILS (admin #2)

MenuAction action = MenuAction.fromMenuNumber(2, UserRole.CUSTOMER);
// Returns: DEPOSIT_MONEY (customer #2)
```

**Same number (2), different actions based on role!**

---

## Complete Menu Reference

### Login Screen

| Component | Value |
|-----------|-------|
| **Type** | Authentication Gateway (pre-menu) |
| **Menu Number** | N/A |
| **Handler** | `AuthenticationManager.login()` |
| **Max Attempts** | 3 |
| **Documentation** | [6.1-Login.md](6.1-Login.md) |

---

### Admin Menu (21 Options + Logout)

**Display Title:** "BANKING MANAGEMENT SYSTEM"

#### Customer Operations (Admin Only)

| Admin # | Option Name | Handler | Category | Doc |
|---------|-------------|---------|----------|-----|
| **1** | Create Customer | `CustomerManager.handleCreateCustomer()` | CUSTOMER_OPERATIONS | [6.2](6.2-Customer-Ops.md#admin-1-create-customer) |
| **2** | View Customer Details | `CustomerManager.handleViewCustomerDetails()` | CUSTOMER_OPERATIONS | [6.2](6.2-Customer-Ops.md#admin-2-view-customer-details) |
| **3** | View All Customers | `CustomerManager.handleViewAllCustomers()` | CUSTOMER_OPERATIONS | [6.2](6.2-Customer-Ops.md#admin-3-view-all-customers) |
| **4** | Delete Customer | `CustomerManager.handleDeleteCustomer()` | CUSTOMER_OPERATIONS | [6.2](6.2-Customer-Ops.md#admin-4-delete-customer) |

#### Account Operations (Mostly Admin)

| Admin # | Customer # | Option Name | Handler | Category | Doc |
|---------|------------|-------------|---------|----------|-----|
| **5** | - | Create Account | `AccountManager.handleCreateAccount()` | ACCOUNT_OPERATIONS | [6.3](6.3-Account-Ops.md#admin-5-create-account) |
| **6** | **1** | View Account Details | `AccountManager.handleViewAccountDetails()` | ACCOUNT_OPERATIONS | [6.3](6.3-Account-Ops.md#admin-6--customer-1-view-account-details) |
| **7** | - | View All Accounts | `AccountManager.handleViewAllAccounts()` | ACCOUNT_OPERATIONS | [6.3](6.3-Account-Ops.md#admin-7-view-all-accounts) |
| **8** | - | Delete Account | `AccountManager.handleDeleteAccount()` | ACCOUNT_OPERATIONS | [6.3](6.3-Account-Ops.md#admin-8-delete-account) |
| **9** | - | Update Overdraft Limit | `AccountManager.handleUpdateOverdraftLimit()` | ACCOUNT_OPERATIONS | [6.3](6.3-Account-Ops.md#admin-9-update-overdraft-limit) |

#### Transaction Operations (Shared)

| Admin # | Customer # | Option Name | Handler | Category | Doc |
|---------|------------|-------------|---------|----------|-----|
| **10** | **2** | Deposit Money | `TransactionProcessor.handleDeposit()` | TRANSACTION_OPERATIONS | [6.4](6.4-Transaction-Ops.md#admin-10--customer-2-deposit-money) |
| **11** | **3** | Withdraw Money | `TransactionProcessor.handleWithdraw()` | TRANSACTION_OPERATIONS | [6.4](6.4-Transaction-Ops.md#admin-11--customer-3-withdraw-money) |
| **12** | **4** | Transfer Money | `TransactionProcessor.handleTransfer()` | TRANSACTION_OPERATIONS | [6.4](6.4-Transaction-Ops.md#admin-12--customer-4-transfer-money) |
| **13** | **5** | View Transaction History | `TransactionProcessor.handleViewTransactionHistory()` | TRANSACTION_OPERATIONS | [6.4](6.4-Transaction-Ops.md#admin-13--customer-5-view-transaction-history) |

#### Profile Operations (Admin Only)

| Admin # | Option Name | Handler | Category | Doc |
|---------|-------------|---------|----------|-----|
| **14** | Create/Update Customer Profile | `CustomerManager.handleCreateCustomerProfile()` | PROFILE_OPERATIONS | [6.5](6.5-Profile-Reports.md#admin-14-createupdate-customer-profile) |
| **15** | Update Profile Information | `CustomerManager.handleUpdateCustomerProfile()` | PROFILE_OPERATIONS | [6.5](6.5-Profile-Reports.md#admin-15-update-profile-information) |

#### Reports & Utilities (Admin Only)

| Admin # | Option Name | Handler | Category | Doc |
|---------|-------------|---------|----------|-----|
| **16** | Apply Interest (Savings) | `AccountManager.handleApplyInterest()` | REPORTS_UTILITIES | [6.5](6.5-Profile-Reports.md#admin-16-apply-interest) |
| **17** | Sort Accounts by Name | `AccountManager.handleSortByName()` | REPORTS_UTILITIES | [6.5](6.5-Profile-Reports.md#admin-17-sort-accounts-by-name) |
| **18** | Sort Accounts by Balance | `AccountManager.handleSortByBalance()` | REPORTS_UTILITIES | [6.5](6.5-Profile-Reports.md#admin-18-sort-accounts-by-balance) |
| **19** | View Audit Trail | `BankingSystem.displayAuditTrail()` | REPORTS_UTILITIES | [6.5](6.5-Profile-Reports.md#admin-19-view-audit-trail) |

#### Security & Session Management

| Admin # | Customer # | Option Name | Handler | Category | Doc |
|---------|------------|-------------|---------|----------|-----|
| **21** | **6** | Change Password | `BankingSystem.handleChangePassword()` | SECURITY_OPERATIONS | [6.6](6.6-Security-Session.md#admin-21--customer-6-change-password) |
| **0** | **0** | Logout | Returns `SESSION_ACTION.LOGOUT` | SESSION_MANAGEMENT | [6.6](6.6-Security-Session.md#admin-0--customer-0-logout) |
| **20** | **7** | Exit Application | Returns `SESSION_ACTION.EXIT_APPLICATION` | SESSION_MANAGEMENT | [6.6](6.6-Security-Session.md#admin-20--customer-7-exit-application) |

---

### Customer Menu (7 Options + Logout)

**Display Title:** "TRANSACTION MENU (ATM Mode)"

| Customer # | Admin # | Option Name | Handler | Category | Doc |
|------------|---------|-------------|---------|----------|-----|
| **1** | 6 | View Account Details | `AccountManager.handleViewAccountDetails()` | ACCOUNT_OPERATIONS | [6.3](6.3-Account-Ops.md#admin-6--customer-1-view-account-details) |
| **2** | 10 | Deposit Money | `TransactionProcessor.handleDeposit()` | TRANSACTION_OPERATIONS | [6.4](6.4-Transaction-Ops.md#admin-10--customer-2-deposit-money) |
| **3** | 11 | Withdraw Money | `TransactionProcessor.handleWithdraw()` | TRANSACTION_OPERATIONS | [6.4](6.4-Transaction-Ops.md#admin-11--customer-3-withdraw-money) |
| **4** | 12 | Transfer Money | `TransactionProcessor.handleTransfer()` | TRANSACTION_OPERATIONS | [6.4](6.4-Transaction-Ops.md#admin-12--customer-4-transfer-money) |
| **5** | 13 | View Transaction History | `TransactionProcessor.handleViewTransactionHistory()` | TRANSACTION_OPERATIONS | [6.4](6.4-Transaction-Ops.md#admin-13--customer-5-view-transaction-history) |
| **6** | 21 | Change Password | `BankingSystem.handleChangePassword()` | SECURITY_OPERATIONS | [6.6](6.6-Security-Session.md#admin-21--customer-6-change-password) |
| **0** | 0 | Logout | Returns `SESSION_ACTION.LOGOUT` | SESSION_MANAGEMENT | [6.6](6.6-Security-Session.md#admin-0--customer-0-logout) |
| **7** | 20 | Exit Application | Returns `SESSION_ACTION.EXIT_APPLICATION` | SESSION_MANAGEMENT | [6.6](6.6-Security-Session.md#admin-20--customer-7-exit-application) |

---

## Access Control Summary

### Admin Permissions (22 Total)

**Full System Access:**
- ✅ All 21 numbered options (1-21)
- ✅ Logout (0)
- ✅ Customer management (Create, View, Update, Delete)
- ✅ Account management for any customer
- ✅ Transactions on any account
- ✅ Profile management
- ✅ System reports (Audit Trail, Sorting, Interest)
- ✅ View all system data

### Customer Permissions (8 Total)

**Limited Access:**
- ✅ View own account details (1)
- ✅ Deposit money (2)
- ✅ Withdraw money (3)
- ✅ Transfer money (4)
- ✅ View transaction history (5)
- ✅ Change password (6)
- ✅ Exit application (7)
- ✅ Logout (0)

**Restrictions:**
- ❌ Cannot create/delete customers or accounts
- ❌ Cannot view other customers' data
- ❌ Cannot perform transactions on others' accounts
- ❌ Cannot view audit trail
- ❌ Cannot apply interest or sort accounts
- ❌ Cannot manage profiles

---

## Documentation Index

### Detailed Menu Documentation

Each menu option is documented in detail across these files:

1. **[6.1-Login.md](6.1-Login.md)** - Login Screen
   Complete authentication flow with 7-step code walkthrough

2. **[6.2-Customer-Ops.md](6.2-Customer-Ops.md)** - Customer Operations
   Admin #1-4: Create, View, Delete customers

3. **[6.3-Account-Ops.md](6.3-Account-Ops.md)** - Account Operations
   Admin #5-9: Account management (Create, View, Delete, Overdraft)

4. **[6.4-Transaction-Ops.md](6.4-Transaction-Ops.md)** - Transaction Operations
   Admin #10-13, Customer #2-5: Money operations (Deposit, Withdraw, Transfer, History)

5. **[6.5-Profile-Reports.md](6.5-Profile-Reports.md)** - Profile & Reports
   Admin #14-19: Profiles, Interest, Sorting, Audit Trail

6. **[6.6-Security-Session.md](6.6-Security-Session.md)** - Security & Session
   Admin #21/#0/#20, Customer #6/#0/#7: Password, Logout, Exit

### Each Detail File Includes:

For every menu option:
- ✅ **Menu Option Details** - Numbers, handler, location, access control
- ✅ **Purpose and Functionality** - What it does and why
- ✅ **7-Step Code Flow** - Detailed execution walkthrough
- ✅ **Code Snippets** - Full implementation with line numbers
- ✅ **Data Flow Diagram** - ASCII visualization
- ✅ **Example Usage** - Success and error scenarios

---

## Menu Display Examples

### Admin Menu Display

```
╔════════════════════════════════════╗
║ BANKING MANAGEMENT SYSTEM          ║
╚════════════════════════════════════╝

┌─ CUSTOMER OPERATIONS ───────────────
│ [1] Create Customer
│ [2] View Customer Details
│ [3] View All Customers
│ [4] Delete Customer
└─────────────────────────────────────

┌─ ACCOUNT OPERATIONS ────────────────
│ [5] Create Account
│ [6] View Account Details
│ [7] View All Accounts
│ [8] Delete Account
│ [9] Update Overdraft Limit (Checking)
└─────────────────────────────────────

┌─ TRANSACTION OPERATIONS ────────────
│ [10] Deposit Money
│ [11] Withdraw Money
│ [12] Transfer Money
│ [13] View Transaction History
└─────────────────────────────────────

... (continues with all 21 options)
```

### Customer Menu Display

```
╔════════════════════════════════════╗
║ TRANSACTION MENU (ATM Mode)       ║
╚════════════════════════════════════╝

┌─ ACCOUNT OPERATIONS ────────────────
│ [1] View Account Details
└─────────────────────────────────────

┌─ TRANSACTION OPERATIONS ────────────
│ [2] Deposit Money
│ [3] Withdraw Money
│ [4] Transfer Money
│ [5] View Transaction History
└─────────────────────────────────────

┌─ SECURITY OPERATIONS ───────────────
│ [6] Change Password
└─────────────────────────────────────

┌─ SESSION MANAGEMENT ────────────────
│ [0] Logout (Return to Login)
│ [7] Exit Application
└─────────────────────────────────────
```

---

## Technical Implementation Highlights

### Enum-Based Architecture

**File:** `src/com/banking/MenuAction.java`

```java
public enum MenuAction {
    // Each action has dual numbering
    DEPOSIT_MONEY("Deposit Money", 10, 2, null, MenuCategory.TRANSACTION_OPERATIONS),
    //             Display Name      ↑   ↑   ↑    Category
    //                          Admin  Cust Role

    // Role-aware menu number lookup
    public int getMenuNumber(UserRole role) {
        return (role == UserRole.CUSTOMER) ? customerMenuNumber : adminMenuNumber;
    }

    // Permission checking
    public boolean canAccess(UserRole userRole) {
        return (requiredRole == null) || (requiredRole == userRole);
    }
}
```

### Auto-Generated Menus

**File:** `src/com/banking/menu/MenuBuilder.java`

The `MenuBuilder` class automatically generates role-specific menus by:
1. Filtering actions available to user's role
2. Grouping by category
3. Sorting by role-specific menu number
4. Formatting with UIFormatter

**Benefits:**
- No manual menu maintenance
- Type-safe (enum-based)
- Single source of truth
- Compile-time validation

---

## Summary Statistics

| Metric | Value |
|--------|-------|
| **Total Menu Actions** | 22 (including login) |
| **Admin Options** | 21 + logout |
| **Customer Options** | 7 + logout |
| **Shared Actions** | 5 (available to both roles) |
| **Admin-Only Actions** | 17 |
| **Menu Categories** | 7 |
| **Handler Classes** | 4 (CustomerManager, AccountManager, TransactionProcessor, AuthenticationManager) |
| **Data Structures Used** | LinkedList (4), Stack (2), ArrayList (1) |
| **Total Documentation Lines** | ~6,150 across 7 files |

---

**Next:** Explore detailed documentation for specific menu options in files 6.1 through 6.6.

