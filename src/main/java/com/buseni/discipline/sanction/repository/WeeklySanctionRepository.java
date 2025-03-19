package com.buseni.discipline.sanction.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.buseni.discipline.sanction.domain.WeeklySanction;

@Repository
public interface WeeklySanctionRepository extends MongoRepository<WeeklySanction, String> {
    
    List<WeeklySanction> findByChildIdOrderByWeekStartDateDesc(String childId);
    
    Optional<WeeklySanction> findByChildIdAndWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(
        String childId, LocalDateTime currentDate, LocalDateTime currentDate2);
    
    List<WeeklySanction> findByParentId(String parentId);
} 