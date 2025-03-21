package com.buseni.discipline.sanction.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanctionHistoryDto {
    private String id;
    private String childId;
    private String childName;
    private String ruleCode;
    private String ruleDescription;
    private int pointsChange;
    private int pointsBefore;
    private int pointsAfter;
    private LocalDateTime appliedAt;
    private String appliedBy;
    private String appliedByName;
} 