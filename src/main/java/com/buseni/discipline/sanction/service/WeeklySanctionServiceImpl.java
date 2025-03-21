package com.buseni.discipline.sanction.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

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
        LocalDateTime now = LocalDateTime.now(PARIS_ZONE);
        
        WeeklySanction currentWeek = weeklySanctionRepository
            .findByChildIdAndWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(childId, now, now)
            .orElseGet(() -> initializeChildWeeklyPoints(childId, now));
        
        RegleDiscipline rule = regleDisciplineService.findByCode(ruleCode)
            .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleCode));
        
        // Create sanction history entry
        SanctionHistory sanctionHistory = new SanctionHistory(
            rule.code(),
            rule.description(),
            rule.points(),
            now,
            appliedBy
        );
        
        // Update points and add sanction to history
        currentWeek.getSanctions().add(sanctionHistory);
        currentWeek.setFinalPoints(currentWeek.getFinalPoints() + rule.points());
        currentWeek.setLastModifiedAt(now);
        currentWeek.setLastModifiedBy(appliedBy);
        
        WeeklySanction saved = weeklySanctionRepository.save(currentWeek);
        return toDto(saved);
    }

    @Override
    public WeeklySanctionDto getCurrentWeekSanction(String childId) {
        LocalDateTime now = LocalDateTime.now(PARIS_ZONE);
        
        return weeklySanctionRepository
            .findByChildIdAndWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(childId, now, now)
            .map(this::toDto)
            .orElseGet(() -> toDto(initializeChildWeeklyPoints(childId, now)));
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
        Child child = childRepository.findById(sanction.getChildId())
            .orElseThrow(() -> new IllegalArgumentException("Child not found: " + sanction.getChildId()));
            
        List<SanctionHistoryDto> sanctionDtos = sanction.getSanctions().stream()
                .map(s -> {
                    String appliedByName = userRepository.findById(s.appliedBy())
                    .map(user -> user.getFirstName() + " " + user.getLastName())
                    .orElse("Unknown");
                    
                return SanctionHistoryDto.builder()
                    .ruleCode(s.ruleCode())
                    .ruleDescription(s.ruleDescription())
                    .pointsChange(s.points())
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
        return weeklySanctionRepository.findByWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(now, now)
            .map(this::toDto)
            .orElseThrow(() -> new IllegalArgumentException("Current week not found"));
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
        public List<WeeklySanctionDto> getRecentActivity(String parentId, int limit) {
        return weeklySanctionRepository.findByParentIdOrderByWeekStartDateDesc(parentId)
            .stream()
            .map(this::toDto)
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