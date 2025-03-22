package com.buseni.discipline.sanction.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.buseni.discipline.children.service.ChildService;
import com.buseni.discipline.sanction.dto.SanctionHistoryDto;
import com.buseni.discipline.sanction.dto.WeeklySanctionDto;
import com.buseni.discipline.sanction.service.WeeklySanctionService;
import com.buseni.discipline.users.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/sanctions")
@RequiredArgsConstructor
@Slf4j
public class SanctionHistoryController {

    private final WeeklySanctionService weeklySanctionService;
    private final ChildService childService;

    @GetMapping("/history")
    public String getSanctionHistory(
            @RequestParam(required = false) String childId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model,
            @AuthenticationPrincipal User user) {
        
        log.debug("Getting sanction history for child: {}, dateFrom: {}, dateTo: {}", childId, dateFrom, dateTo);
        
        // Get children for the current user
        var children = childService.getChildrenByParentId(user.getId());
        model.addAttribute("children", children);
        
        // Set default date range if not provided
        if (dateFrom == null) {
            dateFrom = LocalDate.now().minusMonths(1);
            log.debug("Using default dateFrom: {}", dateFrom);
        }
        
        if (dateTo == null) {
            dateTo = LocalDate.now();
            log.debug("Using default dateTo: {}", dateTo);
        }
        
        // Fetch sanction history
        List<WeeklySanctionDto> weeklySanctions;
        List<SanctionHistoryDto> sanctionHistory = new ArrayList<>();
        
        if (childId != null && !childId.isEmpty()) {
            weeklySanctions = weeklySanctionService.getSanctionHistoryByChildAndDateRange(
                    childId, dateFrom, dateTo, user.getId());
        } else {
            weeklySanctions = weeklySanctionService.getSanctionHistoryByDateRange(
                    dateFrom, dateTo, user.getId());
        }
        
        log.debug("Found {} weekly sanctions", weeklySanctions.size());
        
        // Extract individual sanction history entries for the view
        for (WeeklySanctionDto week : weeklySanctions) {
            log.debug("Processing week: {}, with {} sanctions", week.id(), week.sanctions().size());
            for (SanctionHistoryDto sanction : week.sanctions()) {
                log.debug("Adding sanction: rule={}, points={}, appliedAt={}, childName={}", 
                         sanction.getRuleCode(), sanction.getPointsChange(), 
                         sanction.getAppliedAt(), sanction.getChildName());
                sanctionHistory.add(sanction);
            }
        }
        
        log.debug("Final sanctionHistory size: {}", sanctionHistory.size());
        
        // Sort sanctions by applied date (most recent first)
        sanctionHistory.sort((s1, s2) -> s2.getAppliedAt().compareTo(s1.getAppliedAt()));
        
        if (sanctionHistory.isEmpty()) {
            log.debug("No sanctions found for the specified criteria");
        }
        
        model.addAttribute("sanctionHistory", sanctionHistory);
        model.addAttribute("selectedChildId", childId);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        
        return "sanctions/history";
    }
} 