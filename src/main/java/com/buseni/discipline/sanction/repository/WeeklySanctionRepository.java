package com.buseni.discipline.sanction.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.buseni.discipline.children.domain.Child;
import com.buseni.discipline.sanction.domain.WeeklySanction;


public interface WeeklySanctionRepository extends MongoRepository<WeeklySanction, String> {
    
    List<WeeklySanction> findByChildIdOrderByWeekStartDateDesc(String childId);
    
    Optional<WeeklySanction> findByChildIdAndWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(
        String childId, LocalDateTime currentDate, LocalDateTime currentDate2);
    
    List<WeeklySanction> findByParentId(String parentId);

    Optional<WeeklySanction> findByWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(LocalDateTime now,
            LocalDateTime now2);

    Collection<WeeklySanction> findByParentIdAndWeekNumber(String parentId, Integer weekNumber);

    Collection<WeeklySanction> findByParentIdAndWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(String parentId,
            LocalDateTime now, LocalDateTime now2);

    Collection<WeeklySanction> findByParentIdOrderByWeekStartDateDesc(String parentId);

    Collection<WeeklySanction> findByChildIdAndWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(String childId,
            LocalDate dateFrom, LocalDate dateTo);

    Collection<WeeklySanction> findByChildIdAndParentIdAndWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(
            String childId, String parentId, LocalDate dateFrom, LocalDate dateTo);

    Collection<WeeklySanction> findByParentIdAndWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(String parentId,
            LocalDate dateFrom, LocalDate dateTo);
} 