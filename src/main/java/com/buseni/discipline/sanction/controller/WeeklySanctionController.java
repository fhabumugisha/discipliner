package com.buseni.discipline.sanction.controller;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.buseni.discipline.sanction.dto.WeeklySanctionDto;
import com.buseni.discipline.sanction.service.WeeklySanctionService;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/sanctions")
@RequiredArgsConstructor
public class WeeklySanctionController {

    private final WeeklySanctionService weeklySanctionService;
    
    @GetMapping("/history/{childId}")
    public String getChildSanctionHistory(@PathVariable String childId, Model model) {
        var history = weeklySanctionService.getSanctionHistory(childId);
        model.addAttribute("sanctions", history);
        return "sanctions/history";
    }
    
    @GetMapping("/current/{childId}")
    @HxRequest
    public String getCurrentWeekSanction(@PathVariable String childId, Model model) {
        WeeklySanctionDto currentWeek = weeklySanctionService.getCurrentWeekSanction(childId);
        model.addAttribute("sanction", currentWeek);
        return "sanctions/fragments/current-week :: current-week";
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reset")
    public String manualReset() {
        weeklySanctionService.manualWeeklyReset();
        return "redirect:/sanctions";
    }
    
    @GetMapping("/parent")
    public String getParentSanctions(Principal principal, Model model) {
        var sanctions = weeklySanctionService.getSanctionsByParent(principal.getName());
        model.addAttribute("sanctions", sanctions);
        return "sanctions/parent-view";
    }
} 