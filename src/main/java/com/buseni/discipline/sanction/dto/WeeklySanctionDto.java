package com.buseni.discipline.sanction.dto;

import java.time.LocalDateTime;
import java.util.List;

public record WeeklySanctionDto(
    String id,
    String childId,
    String childName,
    Integer weekNumber,
    Integer year,
    LocalDateTime weekStartDate,
    LocalDateTime weekEndDate,
    Integer initialPoints,
    Integer finalPoints,
    List<SanctionHistoryDto> sanctions,
    LocalDateTime nextResetDate
) {} 