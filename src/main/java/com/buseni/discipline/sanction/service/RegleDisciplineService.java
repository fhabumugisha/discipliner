package com.buseni.discipline.sanction.service;

import java.util.List;
import java.util.Optional;

import com.buseni.discipline.sanction.domain.RegleDiscipline;

/**
 * Service for managing discipline rules.
 */
public interface RegleDisciplineService {
    
    /**
     * Returns all available discipline rules.
     *
     * @return List of all discipline rules
     */
    List<RegleDiscipline> getAllRules();

    /**
     * Finds a discipline rule by its code.
     *
     * @param code The unique code of the rule
     * @return Optional containing the rule if found, empty otherwise
     */
    Optional<RegleDiscipline> findByCode(String code);
} 