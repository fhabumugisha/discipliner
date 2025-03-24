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
     * relative to initial points and the rules severity
     */
    public String getPointsColorClass() {
        if (!hasSanction()) {
            return "text-gray-600 dark:text-gray-400";
        }
        
        int points = sanction.currentPoints();
        int initialPoints = sanction.initialPoints();
        
        // Calculate percentage of points remaining
        double percentRemaining = (double) points / initialPoints * 100;
        
        // Simplified color scheme with 3 colors and better contrast
        if (percentRemaining > 70) {
            return "text-green-600 dark:text-green-400"; // Good (above 70%)
        } else if (percentRemaining >= 50) {
            return "text-yellow-500 dark:text-yellow-400"; // Warning (50-70%) - Bright yellow-orange
        } else {
            return "text-red-700 dark:text-red-500"; // Poor (below 50%) - Deeper red
        }
    }
    
    /**
     * Returns the CSS class for the progress bar based on the current points value
     * relative to initial points and the rules severity
     */
    public String getProgressBarColorClass() {
        if (!hasSanction()) {
            return "bg-gray-500";
        }
        
        int points = sanction.currentPoints();
        int initialPoints = sanction.initialPoints();
        
        // Calculate percentage of points remaining
        double percentRemaining = (double) points / initialPoints * 100;
        
        // Simplified color scheme with 3 colors and better contrast
        if (percentRemaining > 70) {
            return "bg-green-500"; // Good (above 70%)
        } else if (percentRemaining >= 50) {
            return "bg-yellow-500"; // Warning (50-70%) - Bright yellow-orange
        } else {
            return "bg-red-700"; // Poor (below 50%) - Deeper red
        }
    }
} 