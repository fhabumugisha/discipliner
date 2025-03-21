package com.buseni.discipline.sanction.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = (User) userDetails;
        
        // Get children for the current user
        var children = childService.getChildrenByParentId(user.getId());
        model.addAttribute("children", children);
        
        // Set default date range if not provided
        if (dateFrom == null) {
            dateFrom = LocalDate.now().minusMonths(1);
        }
        
        if (dateTo == null) {
            dateTo = LocalDate.now();
        }
        
        // Fetch sanction history
        List<WeeklySanctionDto> sanctionHistory;
        
        if (childId != null && !childId.isEmpty()) {
            sanctionHistory = weeklySanctionService.getSanctionHistoryByChildAndDateRange(
                    childId, dateFrom, dateTo, user.getId());
        } else {
            sanctionHistory = weeklySanctionService.getSanctionHistoryByDateRange(
                    dateFrom, dateTo, user.getId());
        }
        
        model.addAttribute("sanctionHistory", sanctionHistory);
        
        return "sanctions/history";
    }
    
} 