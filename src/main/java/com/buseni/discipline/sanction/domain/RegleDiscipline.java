package com.buseni.discipline.sanction.domain;

/**
 * Represents a discipline rule with a code, description and points value.
 * Points are negative values that will be deducted from a child's total.
 */
public record RegleDiscipline(
    String code,
    String description,
    int points
) {} 