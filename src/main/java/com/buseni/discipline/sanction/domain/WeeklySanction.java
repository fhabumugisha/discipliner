package com.buseni.discipline.sanction.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "weekly_sanctions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklySanction {
    @Id
    private String id;
    
    private String childId;
    private String parentId;
    private Integer weekNumber;
    private Integer year;
    private LocalDateTime weekStartDate;
    private LocalDateTime weekEndDate;
    private Integer initialPoints;
    private Integer finalPoints;
    
    @Builder.Default
    private List<SanctionHistory> sanctions = new ArrayList<>();
    
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime lastModifiedAt;
    private String lastModifiedBy;
} 