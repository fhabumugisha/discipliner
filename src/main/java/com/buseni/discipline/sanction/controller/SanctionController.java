package com.buseni.discipline.sanction.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.buseni.discipline.children.dto.ChildDto;
import com.buseni.discipline.children.service.ChildService;
import com.buseni.discipline.sanction.dto.ChildSanctionViewDto;
import com.buseni.discipline.sanction.dto.WeeklySanctionDto;
import com.buseni.discipline.sanction.service.RegleDisciplineService;
import com.buseni.discipline.sanction.service.WeeklySanctionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/sanctions")
@RequiredArgsConstructor
public class SanctionController {
    // Constants for model attributes
    private static final String CHILDREN_ATTR = "children";
    private static final String WEEKLY_SANCTIONS_ATTR = "weeklySanctions";
    private static final String RULES_ATTR = "rules";
    private static final String CHILD_SANCTIONS = "childSanctions";
    
    // Dependencies
    private final ChildService childService;
    private final WeeklySanctionService weeklySanctionService;
    private final RegleDisciplineService regleDisciplineService;
    
    /**
     * Get the sanctions list page
     */
    @GetMapping
    public String getSanctionsPage(Model model, Principal principal) {
        // Get the children for the current user
        List<ChildDto> children = childService.getChildrenByParentId(principal.getName());
        
        // Get the weekly sanctions for all children for the current week
        List<WeeklySanctionDto> weeklySanctions = children.stream()
            .map(child -> {
                try {
                    return weeklySanctionService.getCurrentWeekSanction(child.getId());
                } catch (Exception e) {
                    log.error("Error getting weekly sanction for child {}: {}", child.getId(), e.getMessage());
                    return null;
                }
            })
            .filter(sanction -> sanction != null)
            .collect(Collectors.toList());
        
        // Create a map for easy lookup of sanctions by child ID
        Map<String, WeeklySanctionDto> sanctionsByChildId = weeklySanctions.stream()
            .collect(Collectors.toMap(WeeklySanctionDto::childId, sanction -> sanction));
        
        // Create a combined DTO for each child with their sanctions
        List<ChildSanctionViewDto> childSanctions = children.stream()
            .map(child -> new ChildSanctionViewDto(
                child, 
                sanctionsByChildId.get(child.getId())
            ))
            .toList();
        
        // Get the rules
        var rules = regleDisciplineService.getAllRules();
        
        // Add everything to the model
        model.addAttribute(CHILDREN_ATTR, children);
        model.addAttribute(WEEKLY_SANCTIONS_ATTR, weeklySanctions);
        model.addAttribute(RULES_ATTR, rules);
        model.addAttribute(CHILD_SANCTIONS, childSanctions);
        
        return "sanctions/list";
    }

    /**
     * Apply a sanction by rule
     */
    @PutMapping("/{childId}/rules/{ruleCode}")
    public String applySanctionByRule(
            @PathVariable String childId,
            @PathVariable String ruleCode,
            Principal principal,
            Model model) {
        
        log.debug("Applying sanction rule {} to child {}", ruleCode, childId);
        
        // Apply the sanction
        WeeklySanctionDto weeklySanction = weeklySanctionService.applySanction(childId, ruleCode, principal.getName());
        
        // Get the child
        ChildDto child = childService.getChildById(childId);
        
        // Get the rules
        var rules = regleDisciplineService.getAllRules();
        
        // Create a ChildSanctionViewDto
        ChildSanctionViewDto childSanction = new ChildSanctionViewDto(child, weeklySanction);
        
        // Add attributes to the model
        model.addAttribute("childSanction", childSanction);
        model.addAttribute("rules", rules);
        
        return "sanctions/fragments/child-points :: points";
    }
    
    /**
     * Apply a manual points adjustment
     */
    @PutMapping("/{childId}/points/{points}")
    public String applySanction(
            @PathVariable String childId,
            @PathVariable Integer points,
            Principal principal,
            Model model) {
        
        log.debug("Applying points {} to child {}", points, childId);
        
        // Apply the points - treat points value as a rule code
        WeeklySanctionDto weeklySanction = weeklySanctionService.applySanction(childId, String.valueOf(points), principal.getName());
        
        // Get the child
        ChildDto child = childService.getChildById(childId);
        
        // Get the rules
        var rules = regleDisciplineService.getAllRules();
        
        // Create a ChildSanctionViewDto
        ChildSanctionViewDto childSanction = new ChildSanctionViewDto(child, weeklySanction);
        
        // Add attributes to the model
        model.addAttribute("childSanction", childSanction);
        model.addAttribute("rules", rules);
        
        return "sanctions/fragments/child-points :: points";
    }
} 