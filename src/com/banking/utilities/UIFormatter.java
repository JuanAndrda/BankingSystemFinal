package com.banking.utilities;

/**
 * UIFormatter Utility Class - Centralized Terminal UI Formatting
 *
 * This utility class provides consistent, professional terminal UI formatting
 * for the Banking Management System. All visual output should use these methods
 * to ensure consistency and maintainability.
 *
 * OOP Principles Demonstrated:
 * - ENCAPSULATION: All UI formatting logic centralized in one place
 * - SINGLE RESPONSIBILITY: Only handles UI formatting (nothing else)
 * - UTILITY PATTERN: Static methods, no instantiation needed
 * - DRY PRINCIPLE: Eliminates 166+ scattered System.out.println statements
 *
 * Design Standards:
 * - Box width: 70 characters (professional terminal standard)
 * - Box style: Double-line characters (╔═╗║╚╝╠╣)
 * - Success marker: ✓ (checkmark)
 * - Error marker: ✗ (X mark)
 * - Info marker: → (arrow)
 * - Bullet: • (bullet point)
 *
 * @author Banking System Team
 * @version 2.0
 */
public class UIFormatter {

    // ===== CONSTANTS =====

    /**
     * Standard box width for all UI elements (70 characters).
     * Chosen for professional appearance and terminal compatibility.
     */
    private static final int BOX_WIDTH = 70;

    /**
     * Content width inside boxes (accounts for border characters).
     * BOX_WIDTH - 2 border characters = 68 characters for content.
     */
    private static final int CONTENT_WIDTH = BOX_WIDTH - 2;

    // Box-drawing characters (Unicode box drawing set)
    private static final char TOP_LEFT = '╔';
    private static final char TOP_RIGHT = '╗';
    private static final char BOTTOM_LEFT = '╚';
    private static final char BOTTOM_RIGHT = '╝';
    private static final char HORIZONTAL = '═';
    private static final char VERTICAL = '║';
    private static final char LEFT_T = '╠';
    private static final char RIGHT_T = '╣';

    // Status and UI markers (public for use by InputValidator)
    public static final String SUCCESS = "✓";
    public static final String ERROR = "✗";
    public static final String INFO = "→";
    public static final String BULLET = "•";

    // Spacing constants
    private static final int BULLET_INDENT = 2;
    private static final int MENU_ITEM_INDENT = 2;

    // ===== PRIVATE CONSTRUCTOR (UTILITY CLASS) =====

    /**
     * Private constructor prevents instantiation.
     * This is a utility class with only static methods.
     */
    private UIFormatter() {
        throw new AssertionError("UIFormatter is a utility class and should not be instantiated");
    }

    // ===== BOX DRAWING METHODS =====

    /**
     * Prints a centered title box (70 characters wide).
     *
     * Example:
     * <pre>
     * ╔══════════════════════════════════════════════════════════════════╗
     * ║                    BANKING MANAGEMENT SYSTEM                     ║
     * ╚══════════════════════════════════════════════════════════════════╝
     * </pre>
     *
     * @param title The title text to display (will be centered)
     */
    public static void printBoxTitle(String title) {
        printTopBorder();
        printCenteredLine(title);
        printBottomBorder();
    }

    /**
     * Prints a section header box (70 characters wide).
     * Title is left-aligned with 2-space indent.
     *
     * Example:
     * <pre>
     * ╔══════════════════════════════════════════════════════════════════╗
     * ║  CREATE CUSTOMER                                                 ║
     * ╚══════════════════════════════════════════════════════════════════╝
     * </pre>
     *
     * @param section The section header text
     */
    public static void printSectionHeader(String section) {
        System.out.println(); // Blank line before section
        printTopBorder();
        printLeftAlignedLine(section, MENU_ITEM_INDENT);
        printBottomBorder();
        System.out.println(); // Blank line after section
    }

    /**
     * Prints a menu section with title and items.
     * Creates a professional box with title bar and menu items.
     *
     * Example:
     * <pre>
     * ╔══════════════════════════════════════════════════════════════════╗
     * ║  CUSTOMER OPERATIONS                                             ║
     * ╠══════════════════════════════════════════════════════════════════╣
     * ║  [1] Create Customer                                             ║
     * ║  [2] View Customer Details                                       ║
     * ║  [3] View All Customers                                          ║
     * ╚══════════════════════════════════════════════════════════════════╝
     * </pre>
     *
     * @param title The section title
     * @param items Array of menu item strings (e.g., "[1] Create Customer")
     */
    public static void printMenuSection(String title, String[] items) {
        printTopBorder();
        printLeftAlignedLine(title, MENU_ITEM_INDENT);
        printMiddleBorder();

        for (String item : items) {
            printLeftAlignedLine(item, MENU_ITEM_INDENT);
        }

        printBottomBorder();
        System.out.println(); // Spacing after section
    }

    // ===== MESSAGE FORMATTING METHODS =====

    /**
     * Prints a success message with checkmark and optional details.
     *
     * Example:
     * <pre>
     * ✓ Customer created successfully!
     *   • Customer ID: C001
     *   • Name: John Doe
     *   • Account: ACC-123
     * </pre>
     *
     * @param message The main success message
     * @param details Optional detail lines (varargs) - each prefixed with bullet
     */
    public static void printSuccess(String message, String... details) {
        System.out.println("\n" + SUCCESS + " " + message);

        if (details != null && details.length > 0) {
            for (String detail : details) {
                printBulletLine(detail);
            }
        }

        System.out.println(); // Blank line after message
    }

    /**
     * Prints an error message with X marker.
     *
     * Example:
     * <pre>
     * ✗ Invalid account number. Please try again.
     * </pre>
     *
     * @param message The error message
     */
    public static void printError(String message) {
        System.out.println("\n" + ERROR + " " + message);
        System.out.println(); // Blank line after error
    }

    /**
     * Prints an info/prompt message with arrow marker.
     * Used for input prompts and informational messages.
     *
     * Example:
     * <pre>
     * → Enter customer ID: _
     * </pre>
     *
     * @param message The prompt/info message
     */
    public static void printPrompt(String message) {
        System.out.print(INFO + " " + message);
    }

    /**
     * Prints a message with arrow marker and newline.
     * Used for informational messages (not input prompts).
     *
     * Example:
     * <pre>
     * → Operation cancelled by user.
     * </pre>
     *
     * @param message The informational message
     */
    public static void printInfo(String message) {
        System.out.println(INFO + " " + message);
    }

    /**
     * Prints a bullet point line (indented).
     * Used for detail lines under success messages.
     *
     * Example:
     * <pre>
     *   • Customer ID: C001
     * </pre>
     *
     * @param text The text to display after the bullet
     */
    public static void printBulletLine(String text) {
        System.out.println(" ".repeat(BULLET_INDENT) + BULLET + " " + text);
    }

    // ===== DATA DISPLAY METHODS =====

    /**
     * Prints a data row in two-column format (label-value).
     * Label is left-aligned, value follows with consistent spacing.
     *
     * Example:
     * <pre>
     * Customer ID:        C001
     * Name:               John Doe
     * Account Type:       Savings
     * </pre>
     *
     * @param label The field label (e.g., "Customer ID:")
     * @param value The field value (e.g., "C001")
     */
    public static void printDataRow(String label, String value) {
        int labelWidth = 20; // Standard label column width
        System.out.printf("%-" + labelWidth + "s%s%n", label, value);
    }

    /**
     * Prints a horizontal separator line (70 characters).
     *
     * Example:
     * <pre>
     * ════════════════════════════════════════════════════════════════════
     * </pre>
     */
    public static void printSeparator() {
        System.out.println(String.valueOf(HORIZONTAL).repeat(BOX_WIDTH));
    }

    /**
     * Prints a blank line (for spacing between sections).
     */
    public static void printBlankLine() {
        System.out.println();
    }

    // ===== BOX HELPER METHODS (PUBLIC FOR MANAGER USE) =====

    /**
     * Prints the top border of a box.
     * Format: ╔══════...══════╗
     */
    public static void printTopBorder() {
        System.out.println(TOP_LEFT + String.valueOf(HORIZONTAL).repeat(CONTENT_WIDTH) + TOP_RIGHT);
    }

    /**
     * Prints the bottom border of a box.
     * Format: ╚══════...══════╝
     */
    public static void printBottomBorder() {
        System.out.println(BOTTOM_LEFT + String.valueOf(HORIZONTAL).repeat(CONTENT_WIDTH) + BOTTOM_RIGHT);
    }

    /**
     * Prints a middle border (divider within a box).
     * Format: ╠══════...══════╣
     */
    public static void printMiddleBorder() {
        System.out.println(LEFT_T + String.valueOf(HORIZONTAL).repeat(CONTENT_WIDTH) + RIGHT_T);
    }

    /**
     * Prints a centered line within a box.
     * Text is centered with padding on both sides.
     *
     * @param text The text to center
     */
    public static void printCenteredLine(String text) {
        int padding = (CONTENT_WIDTH - text.length()) / 2;
        int rightPadding = CONTENT_WIDTH - text.length() - padding;

        System.out.println(
            VERTICAL +
            " ".repeat(padding) +
            text +
            " ".repeat(rightPadding) +
            VERTICAL
        );
    }

    /**
     * Prints a left-aligned line within a box with specified indent.
     *
     * @param text The text to display
     * @param indent Number of spaces to indent from left border
     */
    public static void printLeftAlignedLine(String text, int indent) {
        int availableSpace = CONTENT_WIDTH - indent;
        int rightPadding = availableSpace - text.length();

        // Handle case where text is too long
        if (rightPadding < 0) {
            text = text.substring(0, availableSpace - 3) + "...";
            rightPadding = 0;
        }

        System.out.println(
            VERTICAL +
            " ".repeat(indent) +
            text +
            " ".repeat(rightPadding) +
            VERTICAL
        );
    }

    // ===== UTILITY METHODS FOR SPECIAL CASES =====

    /**
     * Prints a welcome banner for the application start.
     */
    public static void printWelcomeBanner() {
        printBoxTitle("BANKING MANAGEMENT SYSTEM v2.0");
        System.out.println();
    }

    /**
     * Prints a goodbye message for application exit.
     */
    public static void printGoodbyeMessage() {
        System.out.println();
        printBoxTitle("Thank you for using our Banking System!");
        System.out.println();
    }

    /**
     * Prints a divider line with text (for menu sections).
     *
     * Example:
     * <pre>
     * ─────────────── CUSTOMER OPERATIONS ───────────────
     * </pre>
     *
     * @param text The divider text
     */
    public static void printDivider(String text) {
        int dashCount = (BOX_WIDTH - text.length() - 2) / 2;
        String dashes = "─".repeat(dashCount);
        System.out.println(dashes + " " + text + " " + dashes);
    }

    // ===== ENHANCED UI METHODS (Phase 2 Improvements) =====

    /**
     * Prints an enhanced success message with details in a styled box.
     * Provides better visual feedback for successful operations.
     *
     * Example:
     * <pre>
     * ╔══════════════════════════════════════════════════════════════════╗
     * ║  ✓ SUCCESS: Customer created successfully!                      ║
     * ╠══════════════════════════════════════════════════════════════════╣
     * ║    • Customer ID: C001                                           ║
     * ║    • Name: John Doe                                              ║
     * ║    • Status: Active                                              ║
     * ╚══════════════════════════════════════════════════════════════════╝
     * </pre>
     *
     * @param message Main success message
     * @param details Optional detail lines (varargs)
     */
    public static void printSuccessEnhanced(String message, String... details) {
        System.out.println();
        printTopBorder();
        printLeftAlignedLine(SUCCESS + " SUCCESS: " + message, 2);

        if (details != null && details.length > 0) {
            printMiddleBorder();
            for (String detail : details) {
                printLeftAlignedLine("  " + BULLET + " " + detail, 2);
            }
        }

        printBottomBorder();
        System.out.println();
    }

    /**
     * Prints an enhanced error message with optional suggestion in a styled box.
     * Provides better error feedback and user guidance.
     *
     * Example:
     * <pre>
     * ╔══════════════════════════════════════════════════════════════════╗
     * ║  ✗ ERROR: Account does not exist                                ║
     * ╠══════════════════════════════════════════════════════════════════╣
     * ║    Suggestion: Use 'View All Accounts' to see valid numbers     ║
     * ╚══════════════════════════════════════════════════════════════════╝
     * </pre>
     *
     * @param message Error message
     * @param suggestion Optional suggestion for resolving the error (can be null)
     */
    public static void printErrorEnhanced(String message, String suggestion) {
        System.out.println();
        printTopBorder();
        printLeftAlignedLine(ERROR + " ERROR: " + message, 2);

        if (suggestion != null && !suggestion.isEmpty()) {
            printMiddleBorder();
            printLeftAlignedLine("  Suggestion: " + suggestion, 2);
        }

        printBottomBorder();
        System.out.println();
    }

    /**
     * Prints a breadcrumb trail for navigation context.
     * Shows the user's current location in the menu hierarchy.
     *
     * Example:
     * <pre>
     * → Main Menu > Customer Operations > Create Customer
     * </pre>
     *
     * @param breadcrumbs Variable number of breadcrumb strings
     */
    public static void printBreadcrumb(String... breadcrumbs) {
        if (breadcrumbs == null || breadcrumbs.length == 0) {
            return;
        }

        System.out.print("\n" + INFO + " ");
        for (int i = 0; i < breadcrumbs.length; i++) {
            System.out.print(breadcrumbs[i]);
            if (i < breadcrumbs.length - 1) {
                System.out.print(" > ");
            }
        }
        System.out.println("\n");
    }

    /**
     * Prints an operation header with better formatting.
     * Used at the start of each operation for visual consistency.
     *
     * Example:
     * <pre>
     * ╔══════════════════════════════════════════════════════════════════╗
     * ║  CREATE CUSTOMER                                                 ║
     * ╚══════════════════════════════════════════════════════════════════╝
     *
     * </pre>
     *
     * @param operation The operation name
     */
    public static void printOperationHeader(String operation) {
        printSectionHeader(operation);
    }

    /**
     * Prints a warning message (between error and info severity).
     *
     * Example:
     * <pre>
     * ⚠ WARNING: This action cannot be undone
     * </pre>
     *
     * @param message Warning message
     */
    public static void printWarning(String message) {
        System.out.println("\n⚠ WARNING: " + message);
        System.out.println();
    }

    /**
     * Prints a confirmation prompt in a styled box.
     * Used for critical operations that require user confirmation.
     *
     * Example:
     * <pre>
     * ╔══════════════════════════════════════════════════════════════════╗
     * ║  ⚠ CONFIRMATION REQUIRED                                        ║
     * ╠══════════════════════════════════════════════════════════════════╣
     * ║  Are you sure you want to delete account ACC001?                ║
     * ╚══════════════════════════════════════════════════════════════════╝
     * → Your choice (yes/no): _
     * </pre>
     *
     * @param message The confirmation question
     */
    public static void printConfirmationPrompt(String message) {
        System.out.println("\n" + TOP_LEFT + String.valueOf(HORIZONTAL).repeat(CONTENT_WIDTH) + TOP_RIGHT);
        printLeftAlignedLine("⚠ CONFIRMATION REQUIRED", 2);
        printMiddleBorder();
        printLeftAlignedLine(message, 2);
        System.out.println(BOTTOM_LEFT + String.valueOf(HORIZONTAL).repeat(CONTENT_WIDTH) + BOTTOM_RIGHT);
        System.out.print(INFO + " Your choice (yes/no): ");
    }

    /**
     * Prints a loading message (simple text-based indicator).
     * Used for operations that may take time.
     *
     * Example:
     * <pre>
     * → Deleting account...
     * </pre>
     *
     * @param message The operation message
     */
    public static void printLoading(String message) {
        System.out.println("\n" + INFO + " " + message + "...");
    }

    /**
     * Prints a table header for data display.
     * Creates a professional box-style table header.
     *
     * Example:
     * <pre>
     * ╔══════════════════════════════════════════════════════════════════╗
     * ║ Account No     │ Owner              │ Balance                    ║
     * ╠══════════════════════════════════════════════════════════════════╣
     * </pre>
     *
     * @param columns Column headers
     */
    public static void printTableHeader(String... columns) {
        printTopBorder();

        StringBuilder headerRow = new StringBuilder(String.valueOf(VERTICAL));
        int colWidth = (CONTENT_WIDTH - columns.length + 1) / columns.length;

        for (String col : columns) {
            String paddedCol = String.format(" %-" + (colWidth - 1) + "s", col);
            // Truncate if too long
            if (paddedCol.length() > colWidth) {
                paddedCol = paddedCol.substring(0, colWidth - 1) + " ";
            }
            headerRow.append(paddedCol);
            headerRow.append(VERTICAL);
        }

        System.out.println(headerRow);
        printMiddleBorder();
    }

    /**
     * Prints a table row for data display.
     *
     * Example:
     * <pre>
     * ║ ACC001         │ John Doe           │ $5,000.00                  ║
     * </pre>
     *
     * @param values Column values
     */
    public static void printTableRow(String... values) {
        StringBuilder row = new StringBuilder(String.valueOf(VERTICAL));
        int colWidth = (CONTENT_WIDTH - values.length + 1) / values.length;

        for (String val : values) {
            String paddedVal = String.format(" %-" + (colWidth - 1) + "s", val);
            // Truncate if too long
            if (paddedVal.length() > colWidth) {
                paddedVal = paddedVal.substring(0, colWidth - 1) + " ";
            }
            row.append(paddedVal);
            row.append(VERTICAL);
        }

        System.out.println(row);
    }

    /**
     * Closes a table display with bottom border.
     */
    public static void printTableFooter() {
        printBottomBorder();
    }
}
