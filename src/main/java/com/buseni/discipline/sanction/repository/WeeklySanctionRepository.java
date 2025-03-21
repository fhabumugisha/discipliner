package com.buseni.discipline.sanction.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.buseni.discipline.children.domain.Child;
import com.buseni.discipline.sanction.domain.WeeklySanction;


public interface WeeklySanctionRepository extends MongoRepository<WeeklySanction, String> {
    
    List<WeeklySanction> findByChildIdOrderByWeekStartDateDesc(String childId);
    
    Optional<WeeklySanction> findByChildIdAndWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(
        String childId, LocalDateTime currentDate, LocalDateTime currentDate2);
    
    List<WeeklySanction> findByParentId(String parentId);

    Optional<WeeklySanction> findByWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(LocalDateTime now,
            LocalDateTime now2);

    @Query("{'weekStartDate': {$lte: ?0}, 'weekEndDate': {$gte: ?1}}")
    List<WeeklySanction> findAllByWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(
            LocalDateTime currentDate, LocalDateTime currentDate2);

    Collection<WeeklySanction> findByParentIdAndWeekNumber(String parentId, Integer weekNumber);

    Collection<WeeklySanction> findByParentIdAndWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(String parentId,
            LocalDateTime now, LocalDateTime now2);

    Collection<WeeklySanction> findByParentIdOrderByWeekStartDateDesc(String parentId);

    @Query("{'childId': ?0, 'weekStartDate': {$lte: ?1}, 'weekEndDate': {$gte: ?0}}")
    Collection<WeeklySanction> findByChildIdAndWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(String childId,
            LocalDate dateFrom, LocalDate dateTo);

    @Query("{'childId': ?0, 'parentId': ?1, $or: [" +
           "{'weekStartDate': {$gte: ?2, $lte: ?3}}, " +
           "{'weekEndDate': {$gte: ?2, $lte: ?3}}, " +
           "{'weekStartDate': {$lte: ?2}, 'weekEndDate': {$gte: ?3}}]}")
    Collection<WeeklySanction> findByChildIdAndParentIdAndWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(
            String childId, String parentId, LocalDate dateFrom, LocalDate dateTo);

    @Query("{'parentId': ?0, $or: [" +
           "{'weekStartDate': {$gte: ?1, $lte: ?2}}, " +
           "{'weekEndDate': {$gte: ?1, $lte: ?2}}, " +
           "{'weekStartDate': {$lte: ?1}, 'weekEndDate': {$gte: ?2}}]}")
    Collection<WeeklySanction> findByParentIdAndWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(String parentId,
            LocalDate dateFrom, LocalDate dateTo);
} 