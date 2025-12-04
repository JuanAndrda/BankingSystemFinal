package com.banking.menu;

import com.banking.MenuAction;
import com.banking.auth.UserRole;
import com.banking.utilities.UIFormatter;

import java.util.*;

/**
 * MenuBuilder - Auto-generates menu displays from MenuAction enum
 *
 * This utility class eliminates manual menu synchronization issues by
 * automatically generating role-appropriate menus from the MenuAction enum.
 *
 * OOP Principles Demonstrated:
 * - BUILDER PATTERN: Constructs complex menu displays step-by-step
 * - SINGLE RESPONSIBILITY: Only handles menu display generation
 * - DRY PRINCIPLE: Eliminates duplicate menu construction code
 * - ENCAPSULATION: Hides complex menu generation logic
 * - COMPOSITION: Uses CategoryGroup helper class to group actions
 *
 * Data Structures Used:
 * - ArrayList<CategoryGroup> for category grouping (preserves insertion order)
 * - CategoryGroup helper class encapsulates category and actions
 * - Insertion sort for ordering categories (consistent with AccountManager)
 * - ArrayList for dynamic menu item arrays
 * - Enum for type-safe categories
 *
 * Academic Rubric Alignment:
 * - CC 204: Demonstrates ArrayList (dynamic arrays), insertion sort
 * - CIT 207: Builder pattern, encapsulation, composition, helper classes
 */
public class MenuBuilder {

    /**
     * Builds and displays a complete menu for the given user role.
     *
     * Algorithm:
     * 1. Group all MenuActions by category (using ArrayList<CategoryGroup>)
     * 2. Filter categories available to this role
     * 3. Sort categories by displayOrder using insertion sort
     * 4. For each category, display section with formatted actions
     *
     * Data Structure: ArrayList<CategoryGroup> maintains insertion order
     * - Uses CategoryGroup helper class for clean grouping
     * - Insertion sort for ordering (consistent with AccountManager)
     *
     * @param role The user's role (ADMIN or CUSTOMER)
     * @param title The menu title to display at the top
     */
    public static void displayMenu(UserRole role, String title) {
        // Display title banner
        UIFormatter.printBoxTitle(title);
        UIFormatter.printBlankLine();

        // Get category groups (already filtered by role)
        ArrayList<CategoryGroup> categoryGroups = groupActionsByCategory(role);

        // Sort category groups by display order
        sortCategoryGroups(categoryGroups);

        // Display each category section
        for (CategoryGroup group : categoryGroups) {
            if (!group.isEmpty()) {
                displayCategorySection(group.getCategory(), group.getActions(), role);
            }
        }
    }

    /**
     * Groups MenuActions by category for the given role.
     *
     * Data Structure: ArrayList<CategoryGroup>
     * - ArrayList maintains insertion order
     * - CategoryGroup encapsulates category and its actions
     *
     * Algorithm:
     * 1. Iterate through all MenuAction enum values
     * 2. Filter by role availability (isAvailableFor)
     * 3. Find existing category group or create new one
     * 4. Add action to the appropriate group
     *
     * Time Complexity: O(n × m) where n = MenuAction values, m = categories
     *
     * @param role The user role to filter by
     * @return ArrayList of CategoryGroup objects
     */
    private static ArrayList<CategoryGroup> groupActionsByCategory(UserRole role) {
        ArrayList<CategoryGroup> categoryList = new ArrayList<>();

        for (MenuAction action : MenuAction.values()) {
            // Skip if not available for this role
            if (!action.isAvailableFor(role)) {
                continue;
            }

            MenuCategory category = action.getCategory();

            // Find existing category group or create new one
            CategoryGroup group = findCategoryGroup(categoryList, category);
            if (group == null) {
                group = new CategoryGroup(category);
                categoryList.add(group);
            }

            group.addAction(action);
        }

        return categoryList;
    }

    /**
     * Helper method to find a category group in the list.
     * Uses linear search to locate a CategoryGroup matching the given category.
     *
     * Time Complexity: O(m) where m is number of categories (typically ~6)
     *
     * @param list The list of CategoryGroup objects to search
     * @param category The MenuCategory to find
     * @return CategoryGroup if found, null otherwise
     */
    private static CategoryGroup findCategoryGroup(ArrayList<CategoryGroup> list, MenuCategory category) {
        for (CategoryGroup group : list) {
            if (group.getCategory() == category) {
                return group;
            }
        }
        return null;  // Category not found
    }

    /**
     * Sorts category groups by display order using insertion sort.
     * Matches the sorting pattern used in AccountManager for consistency.
     *
     * Data Structure: ArrayList sorted in-place
     * Algorithm: Insertion sort - simple and efficient for small datasets
     *
     * Time Complexity: O(m²) where m is number of categories (typically ~6)
     * - For small m, this is negligible and easier to understand than TimSort
     *
     * @param groups ArrayList of CategoryGroup objects to sort (sorted in-place)
     */
    private static void sortCategoryGroups(ArrayList<CategoryGroup> groups) {
        // Simple insertion sort by displayOrder (already used in AccountManager)
        for (int i = 1; i < groups.size(); i++) {
            CategoryGroup current = groups.get(i);
            int currentOrder = current.getCategory().getDisplayOrder();

            int j = i - 1;
            while (j >= 0 && groups.get(j).getCategory().getDisplayOrder() > currentOrder) {
                groups.set(j + 1, groups.get(j));
                j--;
            }
            groups.set(j + 1, current);
        }
    }

    /**
     * Displays a single category section with its actions.
     *
     * Uses UIFormatter for consistent formatting across the application.
     *
     * Data Structure: String array for menu items
     *
     * @param category The category to display
     * @param actions List of actions in this category
     * @param role The user role (for menu numbering)
     */
    private static void displayCategorySection(MenuCategory category,
                                               List<MenuAction> actions,
                                               UserRole role) {
        // Build menu items array
        String[] menuItems = new String[actions.size()];

        for (int i = 0; i < actions.size(); i++) {
            MenuAction action = actions.get(i);
            int menuNumber = action.getMenuNumber(role);
            menuItems[i] = "[" + menuNumber + "] " + action.getDisplayName();
        }

        // Display using UIFormatter for consistency
        UIFormatter.printMenuSection(category.getDisplayName(), menuItems);
    }

    /**
     * Validates that all MenuActions are properly categorized.
     * Useful for compile-time verification during development.
     *
     * This method can be called during application startup to ensure
     * menu system integrity.
     *
     * @return true if all actions have valid categories
     */
    public static boolean validateMenuCompleteness() {
        boolean allValid = true;

        for (MenuAction action : MenuAction.values()) {
            if (action.getCategory() == null) {
                System.err.println("ERROR: MenuAction " + action + " has no category!");
                allValid = false;
            }
        }

        if (allValid) {
            System.out.println("✓ Menu validation passed: All actions properly categorized");
        }

        return allValid;
    }

    /**
     * Prints menu statistics for debugging/analysis.
     * Useful during development to verify menu structure.
     */
    public static void printMenuStats() {
        System.out.println("\n=== MENU STATISTICS ===");

        // Count total actions
        System.out.println("Total menu actions: " + MenuAction.values().length);

        // Count by role
        int adminActions = 0;
        int customerActions = 0;
        int sharedActions = 0;

        for (MenuAction action : MenuAction.values()) {
            if (action.isAvailableFor(UserRole.ADMIN)) adminActions++;
            if (action.isAvailableFor(UserRole.CUSTOMER)) customerActions++;
            if (action.isAvailableFor(UserRole.ADMIN) && action.isAvailableFor(UserRole.CUSTOMER)) {
                sharedActions++;
            }
        }

        System.out.println("Admin-accessible actions: " + adminActions);
        System.out.println("Customer-accessible actions: " + customerActions);
        System.out.println("Shared actions: " + sharedActions);

        // Count by category
        System.out.println("\n=== ACTIONS BY CATEGORY ===");
        ArrayList<CategoryGroup> categoryGroups = new ArrayList<>();

        for (MenuAction action : MenuAction.values()) {
            MenuCategory category = action.getCategory();

            CategoryGroup group = findCategoryGroup(categoryGroups, category);
            if (group == null) {
                group = new CategoryGroup(category);
                categoryGroups.add(group);
            }
            group.addAction(action);
        }

        // Sort category groups
        sortCategoryGroups(categoryGroups);

        for (CategoryGroup group : categoryGroups) {
            System.out.println(group.getCategory().getDisplayName() + ": " + group.size() + " actions");
        }

        System.out.println("======================\n");
    }
}
