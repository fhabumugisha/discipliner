package com.buseni.discipline.sanction.service;

import java.util.List;

import com.buseni.discipline.sanction.dto.WeeklySanctionDto;

/**
 * Service for managing weekly sanctions.
 */
public interface WeeklySanctionService {
    
    /**
     * Initialize weekly points for all children.
     * This method is called automatically every Monday at first hour
     * or can be triggered manually by an admin.
     */
    void initializeWeeklyPoints();
    
    /**
     * Apply a sanction to a child for the current week.
     * 
     * @param childId the ID of the child
     * @param ruleCode the code of the rule being applied
     * @param appliedBy the ID of the user applying the sanction
     * @return the updated weekly sanction DTO
     */
    WeeklySanctionDto applySanction(String childId, String ruleCode, String appliedBy);
    
    /**
     * Get the current week's sanction for a child.
     * 
     * @param childId the ID of the child
     * @return the current weekly sanction DTO
     */
    WeeklySanctionDto getCurrentWeekSanction(String childId);
    
    /**
     * Get the sanction history for a child.
     * 
     * @param childId the ID of the child
     * @return list of weekly sanction DTOs
     */
    List<WeeklySanctionDto> getSanctionHistory(String childId);
    
    /**
     * Get all sanctions for children of a parent.
     * 
     * @param parentId the ID of the parent
     * @return list of weekly sanction DTOs
     */
    List<WeeklySanctionDto> getSanctionsByParent(String parentId);
    
    /**
     * Manually trigger the weekly points reset.
     * Only accessible by admin users.
     */
    void manualWeeklyReset();
} 