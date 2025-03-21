package com.buseni.discipline.sanction.service;

import java.time.LocalDate;
import java.util.List;

import com.buseni.discipline.sanction.dto.SanctionHistoryDto;
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

    /**
     * Get available weeks for a parent.
     * 
     * @param parentId the ID of the parent
     * @return list of available weeks
     */
    List<WeeklySanctionDto> getAvailableWeeks(String parentId);

    /**
     * Get a week by its ID.
     * 
     * @param weekId the ID of the week
     * @return the week DTO
     */
    WeeklySanctionDto getWeeklySanctionById(String weeklySanctionId);

    /**
     * Get the current week.
     * 
     * @return the current week DTO
     */
    WeeklySanctionDto getCurrentWeek();

    /**
     * Get children summaries for a week.
     * 
     * @param weekId the ID of the week 
     * @param parentId the ID of the parent
     * @return list of children summaries
     */
    List<WeeklySanctionDto> getChildrenSummariesForWeek(Integer weekId, String parentId);

    /**
     * Get the children summaries for the current week.
     * 
     * @param parentId the ID of the parent
     * @return list of children summaries
     */
    List<WeeklySanctionDto> getChildrenSummariesForCurrentWeek(String parentId);

    /**
     * Get recent activity for a parent.
     * 
     * @param parentId the ID of the parent
     * @param limit the limit of recent activity
     * @return list of recent activity
     */
    List<WeeklySanctionDto> getRecentActivity(String parentId, int limit);

    /**
     * Get sanction history by child and date range.
     * 
     * @param childId the ID of the child
     * @param dateFrom the start date
     * @param dateTo the end date
     * @param parentId the ID of the parent
     * @return list of sanction history
     */
    List<WeeklySanctionDto> getSanctionHistoryByChildAndDateRange(String childId, LocalDate dateFrom, LocalDate dateTo, String parentId);

    /**
     * Get sanction history by date range.
     * 
     * @param dateFrom the start date
     * @param dateTo the end date
     * @param parentId the ID of the parent
     * @return list of sanction history
     */
    List<WeeklySanctionDto> getSanctionHistoryByDateRange(LocalDate dateFrom, LocalDate dateTo, String parentId);
} 