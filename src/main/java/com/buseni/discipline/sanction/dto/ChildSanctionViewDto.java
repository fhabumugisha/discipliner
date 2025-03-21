package com.buseni.discipline.sanction.dto;

import com.buseni.discipline.children.dto.ChildDto;

/**
 * Data Transfer Object that combines child and weekly sanction information
 * for display in the UI.
 */
public record ChildSanctionViewDto(ChildDto child, WeeklySanctionDto sanction) {
    
    /**
     * Returns the child ID
     */
    public String getChildId() {
        return child.getId();
    }
    
    /**
     * Returns the child's name
     */
    public String getChildName() {
        return child.getName();
    }
    
    /**
     * Returns the child's age
     */
    public String getChildAge() {
        return child.getAge().toString();
    }
    
    /**
     * Checks if this child has an associated sanction
     */
    public boolean hasSanction() {
        return sanction != null;
    }
    
    /**
     * Returns the current points or a default value if no sanction exists
     */
    public Integer getCurrentPoints() {
        return hasSanction() ? sanction.currentPoints() : 100;
    }
    
    /**
     * Returns the initial points formatted for display
     */
    public String getFormattedInitialPoints() {
        return hasSanction() ? String.valueOf(sanction.initialPoints()) : "—";
    }
    
    /**
     * Returns the points formatted for display
     */
    public String getFormattedPoints() {
        return hasSanction() ? String.valueOf(sanction.currentPoints()) : "—";
    }
    
    /**
     * Returns the CSS class for points based on the current points value
     */
    public String getPointsColorClass() {
        if (!hasSanction()) {
            return "text-gray-600 dark:text-gray-400";
        }
        
        int points = sanction.currentPoints();
        if (points >= 70) {
            return "text-green-600 dark:text-green-400";
        } else if (points >= 40) {
            return "text-yellow-600 dark:text-yellow-400";
        } else {
            return "text-red-600 dark:text-red-400";
        }
    }
    
    /**
     * Returns the CSS class for the progress bar based on the current points value
     */
    public String getProgressBarColorClass() {
        if (!hasSanction()) {
            return "bg-gray-500";
        }
        
        int points = sanction.currentPoints();
        if (points >= 70) {
            return "bg-green-500";
        } else if (points >= 40) {
            return "bg-yellow-500";
        } else {
            return "bg-red-500";
        }
    }
} 