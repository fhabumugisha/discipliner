package com.buseni.discipline.sanction.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.buseni.discipline.children.domain.Child;
import com.buseni.discipline.children.repository.ChildRepository;
import com.buseni.discipline.sanction.domain.RegleDiscipline;
import com.buseni.discipline.sanction.domain.SanctionHistory;
import com.buseni.discipline.sanction.domain.WeeklySanction;
import com.buseni.discipline.sanction.dto.SanctionHistoryDto;
import com.buseni.discipline.sanction.dto.WeeklySanctionDto;
import com.buseni.discipline.sanction.repository.WeeklySanctionRepository;
import com.buseni.discipline.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklySanctionServiceImpl implements WeeklySanctionService {

    private static final int INITIAL_POINTS = 20;
    private static final ZoneId PARIS_ZONE = ZoneId.of("Europe/Paris");
    
    private final WeeklySanctionRepository weeklySanctionRepository;
    private final ChildRepository childRepository;
    private final RegleDisciplineService regleDisciplineService;
    private final UserRepository userRepository;

    @Override
    @Scheduled(cron = "0 0 1 ? * MON", zone = "Europe/Paris") // Every Monday at 1 AM Paris time
    public void initializeWeeklyPoints() {
        log.info("Initializing weekly points for all children");
        
        LocalDateTime now = LocalDateTime.now(PARIS_ZONE);
        WeekFields weekFields = WeekFields.of(Locale.FRANCE);
        int weekNumber = now.get(weekFields.weekOfWeekBasedYear());
        int year = now.getYear();
        
        // Get week start (Monday) and end (Sunday) dates
        LocalDateTime weekStart = now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime weekEnd = now.with(DayOfWeek.SUNDAY).withHour(23).withMinute(59).withSecond(59);
        
        // Initialize points for all children
        List<Child> children = childRepository.findAll();
        for (Child child : children) {
            WeeklySanction weeklySanction = WeeklySanction.builder()
                .childId(child.getId())
                .parentId(child.getParentId())
                .weekNumber(weekNumber)
                .year(year)
                .weekStartDate(weekStart)
                .weekEndDate(weekEnd)
                .initialPoints(INITIAL_POINTS)
                .finalPoints(INITIAL_POINTS)
                .build();
            
            weeklySanctionRepository.save(weeklySanction);
        }
    }

    @Override
    public WeeklySanctionDto applySanction(String childId, String ruleCode, String appliedBy) {
        log.debug("Applying sanction with rule code: {} for child: {} by user: {}", ruleCode, childId, appliedBy);
        LocalDateTime now = LocalDateTime.now(PARIS_ZONE);
        
        // Try to find the current week for this child
        var weekList = weeklySanctionRepository
            .findByChildIdAndWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(childId, now, now);
        
        // If found, use the first result (to handle potential duplicates)
        WeeklySanction currentWeek = weekList.isPresent() 
            ? weekList.get() 
            : initializeChildWeeklyPoints(childId, now);
        
        int pointsChange;
        String ruleDescription = null;
        
        // Handle rule code: if it's a known rule code, use the rule; otherwise try to parse as a points value
        try {
            Optional<RegleDiscipline> ruleOpt = regleDisciplineService.findByCode(ruleCode);
            
            if (ruleOpt.isPresent()) {
                RegleDiscipline rule = ruleOpt.get();
                pointsChange = rule.points();
                ruleDescription = rule.description();
                log.debug("Found rule: {}, points: {}", rule.code(), rule.points());
            } else {
                // If rule not found, try to parse as a direct points value
                try {
                    pointsChange = Integer.parseInt(ruleCode);
                    log.debug("No rule found, using points value: {}", pointsChange);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid rule code or points value: " + ruleCode);
                }
            }
            
            // Create sanction history entry
            SanctionHistory sanctionHistory = new SanctionHistory(
                ruleCode,
                ruleDescription,
                pointsChange,
                now,
                appliedBy
            );
            
            // Add sanction to history and update points
            currentWeek.getSanctions().add(0, sanctionHistory); // Add at beginning for most recent first
            int newPoints = currentWeek.getFinalPoints() + pointsChange;
            currentWeek.setFinalPoints(newPoints);
            currentWeek.setLastModifiedAt(now);
            currentWeek.setLastModifiedBy(appliedBy);
            
            log.debug("Saving weekly sanction. Previous points: {}, New points: {}", 
                     currentWeek.getFinalPoints() - pointsChange, currentWeek.getFinalPoints());
            
            WeeklySanction saved = weeklySanctionRepository.save(currentWeek);
            return toDto(saved);
        } catch (Exception e) {
            log.error("Error applying sanction: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public WeeklySanctionDto getCurrentWeekSanction(String childId) {
        log.debug("Getting current week sanction for child: {}", childId);
        LocalDateTime now = LocalDateTime.now(PARIS_ZONE);
        
        try {
            // Try to find the current week for this child
            var weekList = weeklySanctionRepository
                .findByChildIdAndWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(childId, now, now);
                
            // If found, use the first result (to handle potential duplicates)
            if (weekList.isPresent()) {
                log.debug("Found existing week sanction for child: {}", childId);
                return toDto(weekList.get());
            }
            
            // If no current week exists for this child, initialize one
            log.debug("No current week found for child: {}, initializing a new one", childId);
            return toDto(initializeChildWeeklyPoints(childId, now));
        } catch (Exception e) {
            log.error("Error getting current week sanction for child {}: {}", childId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<WeeklySanctionDto> getSanctionHistory(String childId) {
        return weeklySanctionRepository.findByChildIdOrderByWeekStartDateDesc(childId)
            .stream()
            .map(this::toDto)
            .toList();
    }

    @Override
    public List<WeeklySanctionDto> getSanctionsByParent(String parentId) {
        return weeklySanctionRepository.findByParentId(parentId)
            .stream()
            .map(this::toDto)
            .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void manualWeeklyReset() {
        initializeWeeklyPoints();
    }
    
    private WeeklySanction initializeChildWeeklyPoints(String childId, LocalDateTime now) {
        Child child = childRepository.findById(childId)
            .orElseThrow(() -> new IllegalArgumentException("Child not found: " + childId));
            
        WeekFields weekFields = WeekFields.of(Locale.FRANCE);
        int weekNumber = now.get(weekFields.weekOfWeekBasedYear());
        int year = now.getYear();
        
        // Get week start (Monday) and end (Sunday) dates
        LocalDateTime weekStart = now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime weekEnd = now.with(DayOfWeek.SUNDAY).withHour(23).withMinute(59).withSecond(59);
        
        WeeklySanction weeklySanction = WeeklySanction.builder()
            .childId(childId)
            .parentId(child.getParentId())
            .weekNumber(weekNumber)
            .year(year)
            .weekStartDate(weekStart)
            .weekEndDate(weekEnd)
            .initialPoints(INITIAL_POINTS)
            .finalPoints(INITIAL_POINTS)
            .createdAt(now)
            .build();
            
        return weeklySanctionRepository.save(weeklySanction);
    }
    
    private WeeklySanctionDto toDto(WeeklySanction sanction) {
        try {
            Child child = childRepository.findById(sanction.getChildId())
                .orElseThrow(() -> new IllegalArgumentException("Child not found: " + sanction.getChildId()));
                
            List<SanctionHistoryDto> sanctionDtos = sanction.getSanctions().stream()
                    .map(s -> {
                        String appliedByName = "Unknown";
                        try {
                            appliedByName = userRepository.findById(s.appliedBy())
                                .map(user -> user.getFirstName() + " " + user.getLastName())
                                .orElse("Unknown");
                        } catch (Exception e) {
                            log.warn("Could not find user with ID: {}", s.appliedBy());
                        }
                        
                    return SanctionHistoryDto.builder()
                        .id(sanction.getId() + "_" + s.appliedAt().toString()) // Generate a unique ID
                        .childId(sanction.getChildId())
                        .childName(child.getName())
                        .ruleCode(s.ruleCode())
                        .ruleDescription(s.ruleDescription())
                        .pointsChange(s.points())
                        .pointsBefore(sanction.getFinalPoints() - s.points()) // Approximate previous points
                        .pointsAfter(sanction.getFinalPoints())
                        .appliedAt(s.appliedAt())
                        .appliedBy(s.appliedBy())
                        .appliedByName(appliedByName)
                        .build();       
                })
                .toList();
                
            // Calculate next reset date
            LocalDateTime nextReset = sanction.getWeekEndDate()
                .plusDays(1)
                .withHour(1)
                .withMinute(0)
                .withSecond(0);
                
            return new WeeklySanctionDto(
                sanction.getId(),
                sanction.getChildId(),
                child.getName(),
                sanction.getWeekNumber(),
                sanction.getYear(),
                sanction.getWeekStartDate(),
                sanction.getWeekEndDate(),
                sanction.getInitialPoints(),
                sanction.getFinalPoints(),
                sanctionDtos,
                nextReset
            );
        } catch (Exception e) {
            log.error("Error creating WeeklySanctionDto: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating WeeklySanctionDto", e);
        }
    }

    @Override
    public List<WeeklySanctionDto> getAvailableWeeks(String parentId) {
        return weeklySanctionRepository.findByParentId(parentId)
            .stream()
            .map(this::toDto)
            .toList();
    }

    @Override
    public WeeklySanctionDto getWeeklySanctionById(String weeklySanctionId) {
        return weeklySanctionRepository.findById(weeklySanctionId)
            .map(this::toDto)
            .orElseThrow(() -> new IllegalArgumentException("Week not found: " + weeklySanctionId));
    }

    @Override
    public WeeklySanctionDto getCurrentWeek() {
        LocalDateTime now = LocalDateTime.now(PARIS_ZONE);
        
        // First try to find the current week - use findAll instead of expecting a unique result
        List<WeeklySanction> currentWeeks = weeklySanctionRepository.findAllByWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(now, now);
        
        if (!currentWeeks.isEmpty()) {
            return toDto(currentWeeks.get(0));
        }
        
        // If no current week exists, use the most recent week instead
        return weeklySanctionRepository.findAll().stream()
            .sorted((w1, w2) -> w2.getWeekStartDate().compareTo(w1.getWeekStartDate()))
            .findFirst()
            .map(this::toDto)
            .orElseThrow(() -> new IllegalArgumentException("No weeks found in the system"));
    }   

    @Override
            public List<WeeklySanctionDto> getChildrenSummariesForWeek(Integer weekNumber, String parentId) {
        return weeklySanctionRepository.findByParentIdAndWeekNumber(parentId, weekNumber)
            .stream()
            .map(this::toDto)
            .toList();
    }

    @Override
    public List<WeeklySanctionDto> getChildrenSummariesForCurrentWeek(String parentId) {
        LocalDateTime now = LocalDateTime.now(PARIS_ZONE);
        return weeklySanctionRepository.findByParentIdAndWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(parentId, now, now)
            .stream()
            .map(this::toDto)
            .toList();
    }

    @Override
    public List<SanctionHistoryDto> getRecentActivity(String parentId, int limit) {
        return weeklySanctionRepository.findByParentIdOrderByWeekStartDateDesc(parentId)
            .stream()
            .flatMap(weekly -> weekly.getSanctions().stream()
                .map(sanction -> {
                    String appliedByName = "Unknown";
                    try {
                        appliedByName = userRepository.findById(sanction.appliedBy())
                            .map(user -> user.getFirstName() + " " + user.getLastName())
                            .orElse("Unknown");
                    } catch (Exception e) {
                        log.warn("Could not find user with ID: {}", sanction.appliedBy());
                    }
                    
                    // Get child name
                    String childName = childRepository.findById(weekly.getChildId())
                        .map(Child::getName)
                        .orElse("Unknown");
                    
                    return SanctionHistoryDto.builder()
                        .id(weekly.getId() + "_" + sanction.appliedAt().toString())
                        .childId(weekly.getChildId())
                        .childName(childName)
                        .ruleCode(sanction.ruleCode())
                        .ruleDescription(sanction.ruleDescription())
                        .pointsChange(sanction.points())
                        .pointsBefore(weekly.getFinalPoints() - sanction.points())
                        .pointsAfter(weekly.getFinalPoints())
                        .appliedAt(sanction.appliedAt())
                        .appliedBy(sanction.appliedBy())
                        .appliedByName(appliedByName)
                        .build();
                }))
            .sorted((s1, s2) -> s2.getAppliedAt().compareTo(s1.getAppliedAt()))
            .limit(limit)
            .toList();
    }
    
    @Override
    public List<WeeklySanctionDto> getSanctionHistoryByChildAndDateRange(String childId, LocalDate dateFrom, LocalDate dateTo, String parentId) {
        return weeklySanctionRepository.findByChildIdAndParentIdAndWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(childId, parentId, dateFrom, dateTo)
            .stream()
            .map(this::toDto)
            .toList();
        }       

    @Override
    public List<WeeklySanctionDto> getSanctionHistoryByDateRange(LocalDate dateFrom, LocalDate dateTo, String parentId) {
        return weeklySanctionRepository.findByParentIdAndWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(parentId, dateFrom, dateTo)
            .stream()
            .map(this::toDto)
            .toList();
    }

       
}   