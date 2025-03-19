package com.buseni.discipline.sanction.service;

import com.buseni.discipline.children.dto.ChildDto;

/**
 * Service for managing sanctions.
 */
public interface SanctionService {
    
    /**
     * Applies a sanction to a child by deducting points.
     *
     * @param childId The ID of the child
     * @param points The number of points to deduct (should be negative)
     * @return Updated child DTO
     * @throws IllegalArgumentException if points is positive or child not found
     */
    ChildDto applySanction(String childId, int points);

    /**
     * Applies a predefined sanction rule to a child.
     *
     * @param childId The ID of the child
     * @param ruleCode The code of the sanction rule to apply
     * @return Updated child DTO
     * @throws IllegalArgumentException if rule not found or child not found
     */
    ChildDto applySanctionByRule(String childId, String ruleCode);
} 