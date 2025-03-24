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
        
        // Rule-based classification
        if (percentRemaining >= 90) {
            return "text-green-600 dark:text-green-400"; // Excellent (90-100%)
        } else if (percentRemaining >= 70) {
            return "text-emerald-600 dark:text-emerald-400"; // Very Good (70-90%)
        } else if (percentRemaining >= 50) {
            return "text-yellow-600 dark:text-yellow-400"; // Moderate (50-70%)
        } else if (percentRemaining >= 30) {
            return "text-amber-600 dark:text-amber-400"; // Concerning (30-50%)
        } else if (percentRemaining >= 10) {
            return "text-orange-600 dark:text-orange-400"; // Poor (10-30%)
        } else if (points >= 0) {
            return "text-red-600 dark:text-red-400"; // Very poor but still positive (0-10%)
        } else {
            return "text-red-800 dark:text-red-200"; // Negative points, most severe
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
        
        // Rule-based classification
        if (percentRemaining >= 90) {
            return "bg-green-500"; // Excellent (90-100%)
        } else if (percentRemaining >= 70) {
            return "bg-emerald-500"; // Very Good (70-90%)
        } else if (percentRemaining >= 50) {
            return "bg-yellow-500"; // Moderate (50-70%)
        } else if (percentRemaining >= 30) {
            return "bg-amber-500"; // Concerning (30-50%)
        } else if (percentRemaining >= 10) {
            return "bg-orange-500"; // Poor (10-30%)
        } else if (points >= 0) {
            return "bg-red-500"; // Very poor but still positive (0-10%)
        } else {
            return "bg-red-700"; // Negative points, most severe
        }
    }
} 