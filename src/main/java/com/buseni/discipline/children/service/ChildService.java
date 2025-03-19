package com.buseni.discipline.children.service;

import java.util.List;
import java.util.UUID;

import com.buseni.discipline.children.dto.ChildDto;

/**
 * Service interface for managing children.
 */
public interface ChildService {
    
    /**
     * Get all children for a parent.
     *
     * @param parentId the parent's ID
     * @return list of children DTOs
     */
    List<ChildDto> getChildrenByParentId(UUID parentId);

    /**
     * Create a new child.
     *
     * @param parentId the parent's ID
     * @param childDto the child data
     * @return the created child DTO
     */
    ChildDto createChild(UUID parentId, ChildDto childDto);

    /**
     * Get a child by ID.
     *
     * @param childId the child's ID
     * @return the child DTO
     */
    ChildDto getChildById(String childId);

    /**
     * Update a child.
     *
     * @param childId the child's ID
     * @param childDto the updated child data
     * @return the updated child DTO
     */
    ChildDto updateChild(String childId, ChildDto childDto);
} 