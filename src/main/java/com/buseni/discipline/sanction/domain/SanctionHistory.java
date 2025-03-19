package com.buseni.discipline.sanction.domain;

import java.time.LocalDateTime;

public record SanctionHistory(
    String ruleCode,
    String ruleDescription,
    Integer points,
    LocalDateTime appliedAt,
    String appliedBy
) {} 