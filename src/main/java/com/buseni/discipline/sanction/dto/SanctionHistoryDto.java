package com.buseni.discipline.sanction.dto;

import java.time.LocalDateTime;

public record SanctionHistoryDto(
    String ruleCode,
    String ruleDescription,
    Integer points,
    LocalDateTime appliedAt,
    String appliedBy,
    String appliedByName
) {} 