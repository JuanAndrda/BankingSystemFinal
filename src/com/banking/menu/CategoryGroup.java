package com.banking.menu;

import com.banking.MenuAction;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to group MenuActions by category.
 * This class encapsulates a MenuCategory and its associated MenuActions.
 * It provides a cleaner alternative to using HashMap for grouping operations.
 *
 * OOP Principles Demonstrated:
 * - COMPOSITION: CategoryGroup HAS-A MenuCategory and List<MenuAction>
 * - ENCAPSULATION: Private fields with controlled access via methods
 * - SINGLE RESPONSIBILITY: Only manages category-action grouping
 */
public class CategoryGroup {
    private MenuCategory category;
    private List<MenuAction> actions;

    /**
     * Creates a new CategoryGroup for the specified category.
     * Initializes an empty ArrayList to hold menu actions.
     *
     * @param category The menu category for this group
     */
    public CategoryGroup(MenuCategory category) {
        this.category = category;
        this.actions = new ArrayList<>();
    }

    /**
     * Gets the menu category for this group.
     * @return The MenuCategory
     */
    public MenuCategory getCategory() {
        return category;
    }

    /**
     * Gets the list of menu actions in this category.
     * @return List of MenuAction objects
     */
    public List<MenuAction> getActions() {
        return actions;
    }

    /**
     * Adds a menu action to this category group.
     * @param action The MenuAction to add
     */
    public void addAction(MenuAction action) {
        this.actions.add(action);
    }

    /**
     * Checks if this category group has no actions.
     * @return true if actions list is empty, false otherwise
     */
    public boolean isEmpty() {
        return actions.isEmpty();
    }

    /**
     * Returns the number of actions in this category.
     * @return Number of menu actions
     */
    public int size() {
        return actions.size();
    }
}
