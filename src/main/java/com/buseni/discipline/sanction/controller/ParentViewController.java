package com.buseni.discipline.sanction.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.buseni.discipline.children.service.ChildService;
import com.buseni.discipline.sanction.dto.WeeklySanctionDto;
import com.buseni.discipline.sanction.service.WeeklySanctionService;
import com.buseni.discipline.users.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/sanctions")
@RequiredArgsConstructor
@Slf4j
public class ParentViewController {

    private final WeeklySanctionService weeklySanctionService;
    private final ChildService childService;

    @GetMapping("/parent-view")
    public String getParentView(
            @RequestParam(required = false) String weeklySanctionId,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = (User) userDetails;
        
        // Get children for the current user
        var children = childService.getChildrenByParentId(user.getId());
        
        if (children.isEmpty()) {
            return "redirect:/sanctions";
        }
        
        // Load available weeks
        var availableWeeks = weeklySanctionService.getAvailableWeeks(user.getId());
        model.addAttribute("availableWeeks", availableWeeks);
        model.addAttribute("weeklySanctionId", weeklySanctionId);
        
        // Load selected week data
        WeeklySanctionDto selectedWeek = weeklySanctionId != null && !weeklySanctionId.isEmpty() && !weeklySanctionId.equals("current") 
                ? weeklySanctionService.getWeeklySanctionById(weeklySanctionId)
                : weeklySanctionService.getCurrentWeek();
        
        model.addAttribute("selectedWeek", selectedWeek);
        
        // Load children summaries for the selected week
        var childrenSummaries = weeklySanctionService.getChildrenSummariesForWeek(
                selectedWeek.weekNumber(), user.getId());
        model.addAttribute("childrenSummaries", childrenSummaries);
        
        // Calculate total sanctions for the week
        int totalSanctions = childrenSummaries.stream()
                .mapToInt(summary -> summary.sanctions().size())
                .sum();
        model.addAttribute("totalSanctions", totalSanctions);
        
        // Load recent activity across all children
        var recentActivity = weeklySanctionService.getRecentActivity(user.getId(), 10);
        model.addAttribute("recentActivity", recentActivity);
        
        return "sanctions/parent-view";
    }
} 