# Sorting Algorithms Explained: Insertion Sort vs Merge Sort

## Table of Contents
1. [Introduction](#introduction)
2. [Part 1: Insertion Sort (Current Implementation)](#part-1-insertion-sort-current-implementation)
   - [Overview](#overview)
   - [Line-by-Line Walkthrough: Sort by Name](#line-by-line-walkthrough-sort-by-name)
   - [Visual Example: Sorting Accounts by Name](#visual-example-sorting-accounts-by-name)
   - [Line-by-Line Walkthrough: Sort by Balance](#line-by-line-walkthrough-sort-by-balance)
   - [Visual Example: Sorting Accounts by Balance](#visual-example-sorting-accounts-by-balance)
3. [Part 2: Merge Sort (Alternative Implementation)](#part-2-merge-sort-alternative-implementation)
   - [Overview](#merge-sort-overview)
   - [How Merge Sort Works](#how-merge-sort-works)
   - [Line-by-Line Walkthrough: Merge Sort](#line-by-line-walkthrough-merge-sort)
   - [Visual Example: Merge Sort Process](#visual-example-merge-sort-process)
4. [Part 3: Comparison & Analysis](#part-3-comparison--analysis)
   - [Performance Comparison](#performance-comparison)
   - [When to Use Each Algorithm](#when-to-use-each-algorithm)
   - [LinkedList Considerations](#linkedlist-considerations)
5. [Part 4: Code Implementation Comparison](#part-4-code-implementation-comparison)
6. [Part 5: Academic Context](#part-5-academic-context)

---

## Introduction

This document explains the sorting algorithms used in the Banking Management System project. The current implementation uses **Insertion Sort**, and we'll also explore **Merge Sort** as an alternative approach.

**Current Implementation Location**: `src/com/banking/managers/AccountManager.java` (lines 166-224)

The system provides two sorting operations:
1. **Sort by Customer Name** (alphabetical, ascending)
2. **Sort by Balance** (numerical, descending)

---

# Part 1: Insertion Sort (Current Implementation)

## Overview

**Insertion Sort** is a simple, intuitive sorting algorithm that builds the final sorted array one element at a time. It's similar to how you might sort playing cards in your hand.

### Key Characteristics:
- **Time Complexity**:
  - Best case: O(n) - when array is already sorted
  - Average case: O(n²)
  - Worst case: O(n²) - when array is reverse sorted
- **Space Complexity**: O(1) - sorts in-place, no extra arrays needed
- **Stability**: Yes - maintains relative order of equal elements
- **In-Place**: Yes - modifies the original list

### How It Works:
1. Divide the list into two parts: **sorted** (left) and **unsorted** (right)
2. Initially, the first element is considered sorted
3. For each element in the unsorted part:
   - Take the element
   - Find its correct position in the sorted part
   - Shift larger elements to the right
   - Insert the element at its correct position

### Loop Invariant:
At the start of iteration `i`, the subarray `[0...i-1]` is sorted.

---

## Line-by-Line Walkthrough: Sort by Name

Let's analyze the `insertionSortByName()` method from `AccountManager.java`:

```java
private void insertionSortByName(LinkedList<Account> accountList) {
```
**Purpose**: Sorts accounts alphabetically by customer name (ascending order)
**Parameter**: `accountList` - the LinkedList of Account objects to sort
**Returns**: Nothing (void) - sorts in-place

---

```java
    // For each unsorted element (starting at index 1, first element already "sorted")
    for (int i = 1; i < accountList.size(); i++) {
```
**Line 168 Explanation**:
- **Outer loop** that iterates through the **unsorted portion**
- **Starts at index 1** (not 0) because a single element is already "sorted"
- **`i` represents** the current element we're trying to insert into the sorted portion
- **Loop invariant**: Elements from index 0 to i-1 are sorted
- **Iterations**: If list has 4 elements, loop runs 3 times (i=1, i=2, i=3)

**Example**: For accounts [Charlie, Alice, David, Bob]:
- i=1: Insert "Alice" into [Charlie]
- i=2: Insert "David" into [Alice, Charlie]
- i=3: Insert "Bob" into [Alice, Charlie, David]

---

```java
        // Get current account to insert into sorted portion
        Account currentAccount = accountList.get(i);
```
**Line 170 Explanation**:
- **Extract** the account at position `i` (the next unsorted element)
- **Store** it temporarily in `currentAccount`
- We'll **find the correct position** for this account in the sorted portion
- **LinkedList.get(i)**: O(n) operation for LinkedList (must traverse from head)

**Example**: When i=1, `currentAccount` = Account with owner "Alice"

---

```java
        String currentName = (currentAccount.getOwner() != null)
                ? currentAccount.getOwner().getName() : "";
```
**Lines 171-172 Explanation**:
- **Extract the comparison key**: customer name
- **Null-safety check**: If account has no owner, use empty string
- **Ternary operator**: `condition ? valueIfTrue : valueIfFalse`
- **Chain of calls**: `currentAccount.getOwner().getName()`
  - Get the Customer object (owner)
  - Get the customer's name (String)

**Why extract the name?**
- Avoid repeated method calls inside the while loop
- More efficient to compare strings directly

**Example**: `currentName` = "Alice"

---

```java
        // Find the correct position to insert currentAccount in sorted portion
        int j = i - 1;
```
**Line 175 Explanation**:
- **Initialize pointer `j`** to the last element of the sorted portion
- **`j = i - 1`** because elements [0...i-1] are sorted
- **Purpose**: We'll move backwards through the sorted portion
- **Comparison direction**: Right to left (from end of sorted portion)

**Example**: If i=1, then j=0 (will compare with "Charlie")

---

```java
        while (j >= 0) {
```
**Line 176 Explanation**:
- **Inner loop** that searches backwards through the sorted portion
- **Condition `j >= 0`**: Stop when we reach the beginning of the list
- **Decrements**: `j` decreases each iteration (moving left)
- **Purpose**: Find the correct insertion position

**Two ways the loop can exit**:
1. **`j < 0`**: Reached the beginning (currentAccount is smallest)
2. **`break` statement**: Found the correct position (next line analysis)

---

```java
            Account compareAccount = accountList.get(j);
            String compareName = (compareAccount.getOwner() != null)
                    ? compareAccount.getOwner().getName() : "";
```
**Lines 177-179 Explanation**:
- **Get the account** at position `j` from the sorted portion
- **Extract its name** for comparison (with null-safety)
- **LinkedList.get(j)**: Another O(n) operation (nested inside loops = O(n²) total)

**Example**: When j=0, `compareName` = "Charlie"

---

```java
            // If current name comes before compare name alphabetically, shift right
            if (currentName.compareToIgnoreCase(compareName) < 0) {
```
**Line 182 Explanation**:
- **Compare strings** alphabetically (case-insensitive)
- **`compareToIgnoreCase()`** returns:
  - **Negative** if currentName comes BEFORE compareName ("Alice" < "Charlie")
  - **Zero** if they're equal
  - **Positive** if currentName comes AFTER compareName
- **Condition**: If currentName should go to the LEFT of compareName

**Example**: "Alice".compareToIgnoreCase("Charlie") returns negative (Alice < Charlie)

**Why case-insensitive?**
- "alice" and "Alice" should be treated the same
- User input might have inconsistent capitalization

---

```java
                accountList.set(j + 1, compareAccount);  // Shift element right
```
**Line 183 Explanation**:
- **Shift the larger element** one position to the right
- **Purpose**: Make room for `currentAccount` to be inserted
- **`set(j + 1, compareAccount)`**: Overwrite position j+1 with compareAccount
- **Result**: Creates a "gap" where currentAccount will eventually go

**Visual Example**:
```
Before shift: [Alice, Charlie, ?, David]  (? is old value at j+1)
After shift:  [Alice, Charlie, Charlie, David]  (Charlie copied to j+1)
                            ↑        ↑
                         original  shifted
```

**Important**: We're creating a duplicate temporarily. It will be overwritten when we insert `currentAccount`.

---

```java
                j--;  // Continue searching left
```
**Line 184 Explanation**:
- **Move pointer** one position to the left
- **Purpose**: Check the next element in the sorted portion
- **Continues** the while loop to compare with earlier elements

**Example**: j goes from 0 to -1, which exits the while loop

---

```java
            } else {
                break;  // Found correct position - stop searching
            }
```
**Line 186 Explanation**:
- **Exit the while loop** early
- **Triggered when**: `currentName >= compareName` (current should go to the RIGHT)
- **Reason**: The sorted portion is already sorted, so no need to check further left
- **Optimization**: Stops unnecessary comparisons

**Example**: If we're inserting "David" and we find "Charlie", we know David > Charlie, so David should go to the right of Charlie. No need to check "Alice" and "Bob" to the left.

---

```java
        }

        // Insert current account at its correct sorted position
        accountList.set(j + 1, currentAccount);
    }
}
```
**Line 191 Explanation**:
- **Insert** the currentAccount at position `j + 1`
- **Why `j + 1`?**
  - If loop exited via `break`: `j` points to the last element smaller than current
  - If loop exited via `j < 0`: current is the smallest element, insert at index 0
- **Overwrites** the duplicate created by shifting

**Example**: Insert "Alice" at position 0, overwriting the duplicate "Charlie"

**Result**: Elements [0...i] are now sorted

---

## Visual Example: Sorting Accounts by Name

Let's trace through a complete example with 4 accounts:

### Initial State:
```
Index:     0          1         2         3
Accounts: [Charlie, Alice, David, Bob]
          ↑ sorted    ↑ unsorted
```

---

### **Iteration 1: i = 1** (Insert "Alice")

**Setup**:
```
currentAccount = Alice
currentName = "Alice"
j = 0 (pointing to Charlie)
```

**While Loop**:
```
Step 1: j = 0
  compareAccount = Charlie
  compareName = "Charlie"
  "Alice" < "Charlie"? YES (Alice comes before Charlie alphabetically)
  → Shift Charlie right: set(1, Charlie)
  → j-- (j becomes -1)

Array state: [Charlie, Charlie, David, Bob]
                      ↑ shifted

Step 2: j = -1
  Loop exits (j < 0)
```

**Insert**:
```
set(j + 1, Alice) → set(0, Alice)
Array: [Alice, Charlie, David, Bob]
        ↑ inserted
```

**Result after iteration 1**:
```
[Alice, Charlie] are sorted | [David, Bob] unsorted
```

---

### **Iteration 2: i = 2** (Insert "David")

**Setup**:
```
currentAccount = David
currentName = "David"
j = 1 (pointing to Charlie)
```

**While Loop**:
```
Step 1: j = 1
  compareAccount = Charlie
  compareName = "Charlie"
  "David" < "Charlie"? NO (David > Charlie)
  → break (found correct position)
```

**Insert**:
```
set(j + 1, David) → set(2, David)
Array: [Alice, Charlie, David, Bob]
                        ↑ already in correct position!
```

**Result after iteration 2**:
```
[Alice, Charlie, David] are sorted | [Bob] unsorted
```

---

### **Iteration 3: i = 3** (Insert "Bob")

**Setup**:
```
currentAccount = Bob
currentName = "Bob"
j = 2 (pointing to David)
```

**While Loop**:
```
Step 1: j = 2
  compareAccount = David
  compareName = "David"
  "Bob" < "David"? YES
  → Shift David right: set(3, David)
  → j-- (j becomes 1)

Array state: [Alice, Charlie, David, David]
                                    ↑ shifted

Step 2: j = 1
  compareAccount = Charlie
  compareName = "Charlie"
  "Bob" < "Charlie"? YES
  → Shift Charlie right: set(2, Charlie)
  → j-- (j becomes 0)

Array state: [Alice, Charlie, Charlie, David]
                            ↑ shifted

Step 3: j = 0
  compareAccount = Alice
  compareName = "Alice"
  "Bob" < "Alice"? NO (Bob > Alice)
  → break (found correct position)
```

**Insert**:
```
set(j + 1, Bob) → set(1, Bob)
Array: [Alice, Bob, Charlie, David]
               ↑ inserted
```

**Final Result**:
```
[Alice, Bob, Charlie, David] - fully sorted!
```

---

### Summary of Passes:

| Pass | Element to Insert | Comparisons | Shifts | Result |
|------|------------------|-------------|--------|--------|
| 1    | Alice            | 1 (vs Charlie) | 1 | [Alice, Charlie, David, Bob] |
| 2    | David            | 1 (vs Charlie) | 0 | [Alice, Charlie, David, Bob] |
| 3    | Bob              | 3 (vs David, Charlie, Alice) | 2 | [Alice, Bob, Charlie, David] |

**Total Operations**: 5 comparisons, 3 shifts

---

## Line-by-Line Walkthrough: Sort by Balance

The `insertionSortByBalance()` method is very similar, with one key difference:

```java
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

            // KEY DIFFERENCE: If current balance is GREATER, shift right (descending)
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
```

### Key Differences:

1. **Comparison Key**: Uses `getBalance()` instead of `getName()`
   ```java
   double currentBalance = currentAccount.getBalance();
   ```

2. **Sort Order**: **DESCENDING** instead of ascending
   ```java
   // Line 189: Uses > instead of <
   if (currentBalance > compareBalance) {
   ```
   - **Descending**: Largest balance first (highest to lowest)
   - **Logic**: If current > compare, shift compare right (current should go left)

3. **Data Type**: Compares `double` (numerical) instead of `String` (alphabetical)

---

## Visual Example: Sorting Accounts by Balance

Let's sort accounts by balance in **descending order** (highest first):

### Initial State:
```
Account:   ACC001   ACC002   ACC003   ACC004
Balance:   $150.00  $500.00  $100.00  $300.00
Index:     0        1        2        3
```

---

### **Iteration 1: i = 1** (Insert $500.00)

**Setup**:
```
currentBalance = 500.00
j = 0 (balance: 150.00)
```

**Comparison**:
```
500.00 > 150.00? YES (current is larger, should go to the LEFT)
→ Shift 150.00 right
→ Insert 500.00 at index 0
```

**Result**:
```
Balance: [$500.00, $150.00, $100.00, $300.00]
Index:   0         1         2         3
```

---

### **Iteration 2: i = 2** (Insert $100.00)

**Setup**:
```
currentBalance = 100.00
j = 1 (balance: 150.00)
```

**Comparison**:
```
100.00 > 150.00? NO (current is smaller, should go to the RIGHT)
→ Break (already in correct position)
```

**Result**:
```
Balance: [$500.00, $150.00, $100.00, $300.00]
Index:   0         1         2         3
```

---

### **Iteration 3: i = 3** (Insert $300.00)

**Setup**:
```
currentBalance = 300.00
j = 2 (balance: 100.00)
```

**Comparisons**:
```
Step 1: j = 2
  300.00 > 100.00? YES
  → Shift 100.00 right
  → j = 1

Step 2: j = 1
  300.00 > 150.00? YES
  → Shift 150.00 right
  → j = 0

Step 3: j = 0
  300.00 > 500.00? NO
  → Break (found position)

→ Insert 300.00 at index 1
```

**Final Result**:
```
Balance: [$500.00, $300.00, $150.00, $100.00]  ✓ Descending order!
Index:   0         1         2         3
```

---

# Part 2: Merge Sort (Alternative Implementation)

## Merge Sort Overview

**Merge Sort** is a divide-and-conquer algorithm that divides the list into smaller sublists, sorts them, and merges them back together.

### Key Characteristics:
- **Time Complexity**:
  - Best case: O(n log n)
  - Average case: O(n log n)
  - Worst case: O(n log n) - **CONSISTENT** performance
- **Space Complexity**: O(n) - requires temporary storage
- **Stability**: Yes - maintains relative order
- **In-Place**: No - requires extra space for merging

### Advantages over Insertion Sort:
- **Faster for large datasets**: O(n log n) vs O(n²)
- **Predictable performance**: Always O(n log n), regardless of input order
- **Well-suited for LinkedLists**: No random access needed during merge

### Disadvantages:
- **Extra memory**: Requires O(n) additional space
- **More complex**: Recursive implementation is harder to understand
- **Overhead**: More function calls and array copying

---

## How Merge Sort Works

Merge Sort uses a **divide-and-conquer** strategy with three phases:

### 1. **DIVIDE**: Split the list into two halves
```
[Charlie, Alice, David, Bob]
         ↓ split at middle
[Charlie, Alice] | [David, Bob]
```

### 2. **CONQUER**: Recursively sort each half
```
[Charlie, Alice] → sort → [Alice, Charlie]
[David, Bob]     → sort → [Bob, David]
```

### 3. **COMBINE**: Merge the two sorted halves
```
Merge [Alice, Charlie] and [Bob, David]
  ↓
[Alice, Bob, Charlie, David]
```

### Recursion Tree (4 elements):
```
                [Charlie, Alice, David, Bob]
                           ↓
            ┌──────────────┴──────────────┐
            ↓                             ↓
    [Charlie, Alice]                 [David, Bob]
         ↓                                ↓
    ┌────┴────┐                      ┌────┴────┐
    ↓         ↓                      ↓         ↓
[Charlie]  [Alice]                [David]    [Bob]
    ↓         ↓                      ↓         ↓
    └────┬────┘                      └────┬────┘
         ↓                                ↓
  [Alice, Charlie]                  [Bob, David]
         ↓                                ↓
         └────────────┬───────────────────┘
                      ↓
          [Alice, Bob, Charlie, David]
```

**Depth of tree**: log₂(n) = log₂(4) = 2 levels
**Work per level**: O(n) comparisons and copies
**Total**: O(n log n)

---

## Line-by-Line Walkthrough: Merge Sort

Here's a complete merge sort implementation for the AccountManager:

### Main Sort Method:

```java
private void mergeSortByName(LinkedList<Account> accountList, int left, int right) {
```
**Purpose**: Recursively sorts a portion of the list
**Parameters**:
- `accountList`: The list to sort (modified in-place concept, but uses temp arrays)
- `left`: Starting index of the portion to sort
- `right`: Ending index (inclusive)

**Initial call**: `mergeSortByName(accountList, 0, accountList.size() - 1)`

---

```java
    // Base case: If left >= right, we have 0 or 1 elements (already sorted)
    if (left < right) {
```
**Line explanation**:
- **Base case**: When `left >= right`, the sublist has 0 or 1 elements
- **Single element is sorted**: No work needed, return immediately
- **Recursive case**: When `left < right`, we need to divide and sort

**Example**:
- `left=0, right=0`: Single element, return
- `left=0, right=1`: Two elements, continue dividing

---

```java
        // Find the middle point to divide the list
        int mid = left + (right - left) / 2;
```
**Line explanation**:
- **Calculate midpoint** of the current portion
- **Formula**: `mid = left + (right - left) / 2`
  - Same as `(left + right) / 2` but avoids integer overflow
- **Purpose**: Divide the list into two roughly equal halves

**Example**:
- `left=0, right=3`: mid = 0 + 3/2 = 1 → Split [0-1] and [2-3]
- `left=0, right=1`: mid = 0 + 1/2 = 0 → Split [0] and [1]

---

```java
        // Recursively sort the left half
        mergeSortByName(accountList, left, mid);
```
**Line explanation**:
- **Recursive call** to sort the LEFT half
- **Range**: From `left` to `mid` (inclusive)
- **Effect**: The left half will be sorted when this returns

**Example**: If sorting [0-3], this sorts [0-1]

---

```java
        // Recursively sort the right half
        mergeSortByName(accountList, mid + 1, right);
```
**Line explanation**:
- **Recursive call** to sort the RIGHT half
- **Range**: From `mid + 1` to `right` (inclusive)
- **Effect**: The right half will be sorted when this returns

**Example**: If sorting [0-3], this sorts [2-3]

**Key Point**: After both recursive calls return, we have two **sorted halves** that need to be merged.

---

```java
        // Merge the two sorted halves
        merge(accountList, left, mid, right);
    }
}
```
**Line explanation**:
- **Merge operation**: Combines two sorted halves into one sorted section
- **Parameters**:
  - `left`: Start of first sorted half
  - `mid`: End of first half / Start-1 of second half
  - `right`: End of second sorted half
- **Result**: Range [left...right] is now fully sorted

---

### Merge Method:

```java
private void merge(LinkedList<Account> accountList, int left, int mid, int right) {
```
**Purpose**: Merge two sorted sublists into one sorted list
**Precondition**: [left...mid] and [mid+1...right] are both sorted
**Postcondition**: [left...right] is sorted

---

```java
    // Calculate sizes of the two sublists
    int n1 = mid - left + 1;    // Size of left sublist
    int n2 = right - mid;        // Size of right sublist
```
**Line explanation**:
- **`n1`**: Number of elements in the left half [left...mid]
- **`n2`**: Number of elements in the right half [mid+1...right]

**Example**: If left=0, mid=1, right=3:
- n1 = 1 - 0 + 1 = 2 elements (indices 0, 1)
- n2 = 3 - 1 = 2 elements (indices 2, 3)

---

```java
    // Create temporary arrays to hold the two sublists
    Account[] leftArray = new Account[n1];
    Account[] rightArray = new Account[n2];
```
**Line explanation**:
- **Allocate temporary storage**: O(n) space complexity comes from here
- **Purpose**: Hold copies of the sublists during merging
- **Why needed?**: We'll overwrite elements in `accountList` during merge

---

```java
    // Copy data to temporary arrays
    for (int i = 0; i < n1; i++) {
        leftArray[i] = accountList.get(left + i);
    }
    for (int j = 0; j < n2; j++) {
        rightArray[j] = accountList.get(mid + 1 + j);
    }
```
**Line explanation**:
- **Copy left half**: Elements [left...mid] → leftArray
- **Copy right half**: Elements [mid+1...right] → rightArray
- **O(n) operation**: Copying n elements

**Example**:
```
accountList: [Alice, Charlie, Bob, David]  (indices 0-3, mid=1)
leftArray:   [Alice, Charlie]  (indices 0-1)
rightArray:  [Bob, David]      (indices 2-3)
```

---

```java
    // Merge the two sublists back into accountList
    int i = 0;     // Index for leftArray
    int j = 0;     // Index for rightArray
    int k = left;  // Index for merged accountList
```
**Line explanation**:
- **Three pointers**:
  - `i`: Current position in leftArray (starts at 0)
  - `j`: Current position in rightArray (starts at 0)
  - `k`: Current position in accountList where we'll place the next element (starts at `left`)

**Two-pointer technique**: Compare elements from both arrays and pick the smaller one.

---

```java
    while (i < n1 && j < n2) {
```
**Line explanation**:
- **Merge loop**: Runs while BOTH arrays have elements remaining
- **Condition**: Stop when we've exhausted either left or right array

---

```java
        // Get names for comparison
        String leftName = leftArray[i].getOwner() != null
                ? leftArray[i].getOwner().getName() : "";
        String rightName = rightArray[j].getOwner() != null
                ? rightArray[j].getOwner().getName() : "";
```
**Line explanation**:
- **Extract comparison keys** (customer names)
- **Null-safety**: Handle accounts without owners

---

```java
        // Compare and pick the smaller element (alphabetically first)
        if (leftName.compareToIgnoreCase(rightName) <= 0) {
            accountList.set(k, leftArray[i]);
            i++;  // Move to next element in left array
        } else {
            accountList.set(k, rightArray[j]);
            j++;  // Move to next element in right array
        }
        k++;  // Move to next position in merged list
```
**Line explanation**:
- **Compare**: Which element should come first alphabetically?
- **If left <= right**:
  - Place `leftArray[i]` at position `k`
  - Advance `i` pointer (used up this element from left)
- **If right < left**:
  - Place `rightArray[j]` at position `k`
  - Advance `j` pointer (used up this element from right)
- **Always advance `k`**: We filled one position in the merged list

**Example**:
```
leftArray:  [Alice, Charlie]  i=0 → "Alice"
rightArray: [Bob, David]      j=0 → "Bob"

Compare: "Alice" <= "Bob"? YES
→ accountList[k] = Alice
→ i++, k++

Next iteration:
leftArray:  [Alice, Charlie]  i=1 → "Charlie"
rightArray: [Bob, David]      j=0 → "Bob"

Compare: "Charlie" <= "Bob"? NO
→ accountList[k] = Bob
→ j++, k++
```

---

```java
    }

    // Copy remaining elements from leftArray (if any)
    while (i < n1) {
        accountList.set(k, leftArray[i]);
        i++;
        k++;
    }
```
**Line explanation**:
- **Handle leftover elements** from leftArray
- **When this runs**: When rightArray is exhausted but leftArray still has elements
- **Why needed**: The first while loop stops when one array is empty

**Example**: If leftArray has [Charlie, David] and rightArray is empty, copy both to accountList.

---

```java
    // Copy remaining elements from rightArray (if any)
    while (j < n2) {
        accountList.set(k, rightArray[j]);
        j++;
        k++;
    }
}
```
**Line explanation**:
- **Handle leftover elements** from rightArray
- **When this runs**: When leftArray is exhausted but rightArray still has elements

**Note**: At most ONE of these cleanup loops will execute (never both).

---

## Visual Example: Merge Sort Process

Let's sort the same 4 accounts: [Charlie, Alice, David, Bob]

### **Level 1: Initial Call**
```
mergeSortByName(list, 0, 3)
  left=0, right=3, mid=1
```

### **Level 2: Divide**
```
Call 1: mergeSortByName(list, 0, 1)  ← Sort left half
  left=0, right=1, mid=0

Call 2: mergeSortByName(list, 2, 3)  ← Sort right half
  left=2, right=3, mid=2
```

### **Level 3: Base Cases** (Single Elements)
```
From Call 1:
  mergeSortByName(list, 0, 0)  ← [Charlie] (single element, return)
  mergeSortByName(list, 1, 1)  ← [Alice] (single element, return)

From Call 2:
  mergeSortByName(list, 2, 2)  ← [David] (single element, return)
  mergeSortByName(list, 3, 3)  ← [Bob] (single element, return)
```

### **Level 3: Merge** (Pairs)
```
Merge [Charlie] and [Alice]:
  leftArray = [Charlie]
  rightArray = [Alice]

  Compare: "Charlie" vs "Alice" → "Alice" < "Charlie"
  Result: [Alice, Charlie]

Merge [David] and [Bob]:
  leftArray = [David]
  rightArray = [Bob]

  Compare: "David" vs "Bob" → "Bob" < "David"
  Result: [Bob, David]
```

**State after Level 3**:
```
[Alice, Charlie, Bob, David]
 ↑____________↑  ↑________↑
   sorted         sorted
```

### **Level 2: Final Merge**
```
Merge [Alice, Charlie] and [Bob, David]:
  leftArray = [Alice, Charlie]
  rightArray = [Bob, David]

  i=0, j=0, k=0

  Step 1:
    Compare: "Alice" vs "Bob" → "Alice" < "Bob"
    accountList[0] = Alice
    i=1, k=1

  Step 2:
    Compare: "Charlie" vs "Bob" → "Bob" < "Charlie"
    accountList[1] = Bob
    j=1, k=2

  Step 3:
    Compare: "Charlie" vs "David" → "Charlie" < "David"
    accountList[2] = Charlie
    i=2, k=3

  Step 4:
    leftArray exhausted (i=2, n1=2)
    Copy remaining from rightArray:
    accountList[3] = David
    j=2, k=4
```

**Final Result**:
```
[Alice, Bob, Charlie, David]  ✓ Sorted!
```

### Recursion Tree with States:

```
                     [Charlie, Alice, David, Bob]
                              ↓ split
              ┌───────────────┴───────────────┐
              ↓                               ↓
      [Charlie, Alice]                   [David, Bob]
           ↓ split                          ↓ split
     ┌─────┴─────┐                    ┌─────┴─────┐
     ↓           ↓                    ↓           ↓
 [Charlie]   [Alice]              [David]      [Bob]
  (base)     (base)               (base)       (base)
     ↓           ↓                    ↓           ↓
     └─────┬─────┘                    └─────┬─────┘
           ↓ merge                          ↓ merge
   [Alice, Charlie]                    [Bob, David]
           ↓                                ↓
           └────────────┬───────────────────┘
                        ↓ merge
            [Alice, Bob, Charlie, David]
```

**Total Work**:
- **Depth**: log₂(4) = 2 levels
- **Comparisons per level**: ~4 comparisons
- **Total comparisons**: 2 × 4 = 8 (vs insertion sort's 5 for this example)

**Note**: For this small example, insertion sort is actually faster! Merge sort's advantage shows with larger datasets.

---

# Part 3: Comparison & Analysis

## Performance Comparison

| **Metric** | **Insertion Sort** | **Merge Sort** |
|------------|-------------------|----------------|
| **Best Case Time** | O(n) - already sorted | O(n log n) - always splits |
| **Average Case Time** | O(n²) - random order | O(n log n) - consistent |
| **Worst Case Time** | O(n²) - reverse sorted | O(n log n) - consistent |
| **Space Complexity** | O(1) - in-place | O(n) - temp arrays |
| **Stability** | Yes - equal elements maintain order | Yes - equal elements maintain order |
| **In-Place** | Yes - no extra arrays | No - requires temporary storage |
| **Adaptive** | Yes - faster on partially sorted data | No - always same performance |
| **Number of Comparisons** | 0 to n²/2 | n log n |
| **Number of Swaps** | 0 to n²/2 | n log n |
| **Cache Performance** | Good - sequential access | Moderate - random during merge |
| **Recursive** | No - iterative | Yes - recursive calls |

### Concrete Example (n = 1000 accounts):

| **Algorithm** | **Best Case** | **Average Case** | **Worst Case** |
|---------------|---------------|------------------|----------------|
| **Insertion Sort** | 1,000 operations | 500,000 operations | 1,000,000 operations |
| **Merge Sort** | 10,000 operations | 10,000 operations | 10,000 operations |

**Speedup**: Merge sort is **50-100x faster** for n=1000!

---

## When to Use Each Algorithm

### Use **Insertion Sort** When:

1. **Small datasets** (< 50 elements)
   - Example: Sorting 10 bank accounts in a small branch
   - Insertion sort has lower overhead (no recursion, no extra arrays)

2. **Nearly sorted data**
   - Example: Adding a few new accounts to an already sorted list
   - Best case O(n) when data is already sorted

3. **Memory is limited**
   - Example: Embedded systems, IoT devices
   - O(1) space vs O(n) for merge sort

4. **Simplicity is important**
   - Example: Teaching algorithm basics
   - Easier to understand and implement

5. **Online sorting** (elements arrive one at a time)
   - Example: Real-time transaction processing
   - Can insert new elements efficiently into sorted portion

### Use **Merge Sort** When:

1. **Large datasets** (> 100 elements)
   - Example: Sorting 10,000 customer accounts
   - O(n log n) vs O(n²) makes huge difference

2. **Predictable performance is critical**
   - Example: Systems with real-time constraints
   - Always O(n log n), regardless of input order

3. **Stability is required**
   - Example: Sorting by multiple criteria (sort by balance, then by name)
   - Both are stable, but merge sort is faster for large data

4. **Linked lists are used**
   - Example: Your current implementation uses LinkedList
   - Merge sort doesn't need random access (no `get(i)` calls in merge)

5. **Parallel processing is available**
   - Example: Multi-core systems
   - Merge sort can be parallelized easily (divide-and-conquer)

---

## LinkedList Considerations

### Current Implementation Issue:
```java
for (int i = 1; i < accountList.size(); i++) {
    Account currentAccount = accountList.get(i);  // ❌ O(n) for LinkedList
    // ...
    for (int j = i - 1; j >= 0; j--) {
        Account compareAccount = accountList.get(j);  // ❌ O(n) for LinkedList
    }
}
```

**Problem**: `LinkedList.get(i)` is **O(n)** because it must traverse from the head.

**Time Complexity Analysis**:
- Outer loop: n iterations
- Inner loop: n iterations (worst case)
- Each `get()`: O(n) traversal
- **Total**: O(n) × O(n) × O(n) = **O(n³)** ❌

**Actual performance** is closer to O(n²) due to average-case `get()` being O(n/2), but still inefficient.

---

### Solutions:

#### **Solution 1**: Convert to ArrayList for sorting
```java
public void sortAccountsByName() {
    ArrayList<Account> tempList = new ArrayList<>(this.accountList);
    insertionSortByName(tempList);  // O(n²) with O(1) access
    this.accountList.clear();
    this.accountList.addAll(tempList);
}
```
**Pros**: Fast sorting with O(1) random access
**Cons**: O(n) space for temporary ArrayList

#### **Solution 2**: Use Merge Sort (no random access needed)
```java
public void sortAccountsByName() {
    mergeSortByName(this.accountList, 0, this.accountList.size() - 1);
}
```
**Pros**:
- O(n log n) time complexity
- Doesn't rely on random access
- Can use iterators for LinkedList traversal

**Cons**: O(n) space for temporary arrays

#### **Solution 3**: Use Collections.sort()
```java
public void sortAccountsByName() {
    Collections.sort(this.accountList, (a1, a2) -> {
        String name1 = a1.getOwner() != null ? a1.getOwner().getName() : "";
        String name2 = a2.getOwner() != null ? a2.getOwner().getName() : "";
        return name1.compareToIgnoreCase(name2);
    });
}
```
**Pros**:
- Uses optimized Timsort (hybrid merge sort + insertion sort)
- Automatically chooses best algorithm
- One line of code!

**Cons**: Doesn't demonstrate algorithm knowledge for academic purposes

---

### Why LinkedList is Wrong for This Use Case:

| **Operation** | **ArrayList** | **LinkedList** | **Winner** |
|---------------|---------------|----------------|------------|
| `get(i)` | O(1) | O(n) | ArrayList |
| `add(element)` | O(1) amortized | O(1) | Tie |
| `remove(i)` | O(n) | O(n) | Tie |
| **Sorting** | O(n²) or O(n log n) | O(n³) or O(n²) | ArrayList |

**Conclusion**: Unless you're frequently adding/removing at the head/tail, **ArrayList is better**.

---

# Part 4: Code Implementation Comparison

## Current Implementation: Insertion Sort

### Sort by Name (Ascending):
```java
private void insertionSortByName(LinkedList<Account> accountList) {
    for (int i = 1; i < accountList.size(); i++) {
        Account currentAccount = accountList.get(i);
        String currentName = (currentAccount.getOwner() != null)
                ? currentAccount.getOwner().getName() : "";

        int j = i - 1;
        while (j >= 0) {
            Account compareAccount = accountList.get(j);
            String compareName = (compareAccount.getOwner() != null)
                    ? compareAccount.getOwner().getName() : "";

            if (currentName.compareToIgnoreCase(compareName) < 0) {
                accountList.set(j + 1, compareAccount);
                j--;
            } else {
                break;
            }
        }

        accountList.set(j + 1, currentAccount);
    }
}
```

### Sort by Balance (Descending):
```java
private void insertionSortByBalance(LinkedList<Account> accountList) {
    for (int i = 1; i < accountList.size(); i++) {
        Account currentAccount = accountList.get(i);
        double currentBalance = currentAccount.getBalance();

        int j = i - 1;
        while (j >= 0) {
            Account compareAccount = accountList.get(j);
            double compareBalance = compareAccount.getBalance();

            if (currentBalance > compareBalance) {  // Descending: > instead of <
                accountList.set(j + 1, compareAccount);
                j--;
            } else {
                break;
            }
        }

        accountList.set(j + 1, currentAccount);
    }
}
```

**Lines of Code**: ~20 lines per method
**Complexity**: Low - easy to understand
**Performance**: O(n²) average case

---

## Alternative Implementation: Merge Sort

### Sort by Name (Ascending):
```java
private void mergeSortByName(LinkedList<Account> accountList, int left, int right) {
    if (left < right) {
        int mid = left + (right - left) / 2;

        // Recursively sort left and right halves
        mergeSortByName(accountList, left, mid);
        mergeSortByName(accountList, mid + 1, right);

        // Merge the sorted halves
        mergeByName(accountList, left, mid, right);
    }
}

private void mergeByName(LinkedList<Account> accountList, int left, int mid, int right) {
    // Calculate sizes
    int n1 = mid - left + 1;
    int n2 = right - mid;

    // Create temp arrays
    Account[] leftArray = new Account[n1];
    Account[] rightArray = new Account[n2];

    // Copy data to temp arrays
    for (int i = 0; i < n1; i++) {
        leftArray[i] = accountList.get(left + i);
    }
    for (int j = 0; j < n2; j++) {
        rightArray[j] = accountList.get(mid + 1 + j);
    }

    // Merge the temp arrays back
    int i = 0, j = 0, k = left;

    while (i < n1 && j < n2) {
        String leftName = leftArray[i].getOwner() != null
                ? leftArray[i].getOwner().getName() : "";
        String rightName = rightArray[j].getOwner() != null
                ? rightArray[j].getOwner().getName() : "";

        if (leftName.compareToIgnoreCase(rightName) <= 0) {
            accountList.set(k, leftArray[i]);
            i++;
        } else {
            accountList.set(k, rightArray[j]);
            j++;
        }
        k++;
    }

    // Copy remaining elements
    while (i < n1) {
        accountList.set(k, leftArray[i]);
        i++;
        k++;
    }

    while (j < n2) {
        accountList.set(k, rightArray[j]);
        j++;
        k++;
    }
}

// Public method to call merge sort
public void sortAccountsByName() {
    if (!this.accountList.isEmpty()) {
        mergeSortByName(this.accountList, 0, this.accountList.size() - 1);
        UIFormatter.printSuccess("Accounts sorted by customer name");
    }
}
```

### Sort by Balance (Descending):
```java
private void mergeSortByBalance(LinkedList<Account> accountList, int left, int right) {
    if (left < right) {
        int mid = left + (right - left) / 2;

        mergeSortByBalance(accountList, left, mid);
        mergeSortByBalance(accountList, mid + 1, right);

        mergeByBalance(accountList, left, mid, right);
    }
}

private void mergeByBalance(LinkedList<Account> accountList, int left, int mid, int right) {
    int n1 = mid - left + 1;
    int n2 = right - mid;

    Account[] leftArray = new Account[n1];
    Account[] rightArray = new Account[n2];

    for (int i = 0; i < n1; i++) {
        leftArray[i] = accountList.get(left + i);
    }
    for (int j = 0; j < n2; j++) {
        rightArray[j] = accountList.get(mid + 1 + j);
    }

    int i = 0, j = 0, k = left;

    while (i < n1 && j < n2) {
        double leftBalance = leftArray[i].getBalance();
        double rightBalance = rightArray[j].getBalance();

        // Descending order: >= instead of <=
        if (leftBalance >= rightBalance) {
            accountList.set(k, leftArray[i]);
            i++;
        } else {
            accountList.set(k, rightArray[j]);
            j++;
        }
        k++;
    }

    while (i < n1) {
        accountList.set(k, leftArray[i]);
        i++;
        k++;
    }

    while (j < n2) {
        accountList.set(k, rightArray[j]);
        j++;
        k++;
    }
}

public void sortAccountsByBalance() {
    if (!this.accountList.isEmpty()) {
        mergeSortByBalance(this.accountList, 0, this.accountList.size() - 1);
        UIFormatter.printSuccess("Accounts sorted by balance (descending)");
    }
}
```

**Lines of Code**: ~60 lines per method
**Complexity**: High - recursive logic
**Performance**: O(n log n) all cases

---

## Key Differences:

| **Aspect** | **Insertion Sort** | **Merge Sort** |
|------------|-------------------|----------------|
| **Method count** | 1 method | 2 methods (sort + merge) |
| **Recursion** | No - iterative loops | Yes - divide and conquer |
| **Temporary storage** | None | Arrays for left and right |
| **Comparison location** | In-place within list | In merge phase |
| **Code readability** | High - straightforward | Medium - recursive logic |
| **Debugging ease** | Easy - step through loops | Harder - track recursion |

---

# Part 5: Academic Context

## Why Insertion Sort Was Chosen

For an **academic banking project**, insertion sort is an excellent choice because:

### 1. **Learning Objectives**
- **Demonstrates fundamental concepts**:
  - Loop invariants (elements [0...i-1] are sorted)
  - In-place sorting (no extra space)
  - Comparison-based sorting
  - Best/average/worst case analysis

- **Shows OOP integration**:
  - Sorting custom objects (Account)
  - Using getters (getBalance(), getOwner())
  - Null-safety checks
  - String vs numerical comparisons

### 2. **Appropriate for Expected Data Size**
- **Small datasets**: Banking systems typically have < 1000 accounts per branch
- **O(n²) is acceptable**: For n=100, that's only 10,000 operations
- **Real-world context**: Most banking operations involve small result sets

### 3. **Code Simplicity**
- **Easy to implement**: ~20 lines per method
- **Easy to explain**: Can trace through by hand
- **Easy to debug**: No recursive calls to track
- **Easy to test**: Can verify correctness visually

### 4. **Demonstrates Algorithm Design Choices**
- **Ascending vs Descending**: Shows how to modify comparison logic
- **Different data types**: Sorting by String vs double
- **Custom comparators**: Using compareToIgnoreCase()

---

## When to Upgrade to Merge Sort

Consider upgrading to merge sort if:

1. **Dataset grows** beyond 100-200 accounts
2. **Performance becomes critical** (user-facing sorting operations)
3. **Demonstrating advanced algorithms** is a learning objective
4. **Moving toward production** deployment

---

## Trade-offs in Algorithm Selection

### Insertion Sort Advantages:
✓ Simple implementation
✓ Low memory overhead (O(1))
✓ Fast for small datasets
✓ Adaptive (fast on nearly sorted data)
✓ Easy to understand and maintain

### Insertion Sort Disadvantages:
✗ Slow for large datasets (O(n²))
✗ Inefficient with LinkedList (O(n³))
✗ No parallelization possible

### Merge Sort Advantages:
✓ Consistent O(n log n) performance
✓ Scalable to large datasets
✓ Works well with LinkedList
✓ Can be parallelized
✓ Stable sorting

### Merge Sort Disadvantages:
✗ More complex implementation
✗ Higher memory usage (O(n))
✗ Recursive overhead
✗ Slower for small datasets

---

## Conclusion

For this **academic banking project**, **insertion sort is the right choice** because:
- Dataset is small (demo data has ~7 accounts)
- Code clarity is important for grading
- Demonstrates fundamental algorithm concepts
- Performance is acceptable for the use case

However, understanding **merge sort as an alternative** shows:
- Knowledge of algorithm complexity analysis
- Ability to choose appropriate algorithms for scale
- Understanding of trade-offs between time and space

**Best Practice for Production**: Use `Collections.sort()` which implements **Timsort** (hybrid of merge sort and insertion sort) - combining the best of both worlds!

---

## Further Reading

1. **Big O Notation**: Understanding time and space complexity
2. **Stability in Sorting**: Why it matters for multi-key sorting
3. **Comparison vs Non-Comparison Sorts**: Radix, Bucket, Counting sorts
4. **Hybrid Algorithms**: Timsort (Python, Java), Introsort (C++)
5. **Cache Performance**: How memory access patterns affect real-world performance

---

**Document created for**: Banking Management System (BankingProjectPart3)
**Author**: Claude Code
**Date**: 2025-12-10
**Purpose**: Educational documentation explaining sorting algorithm implementations
