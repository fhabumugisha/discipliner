package com.buseni.discipline.sanction.controller;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.buseni.discipline.users.domain.User;

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
    private static final String CHILD_SANCTION = "childSanction";           
    private static final String ERROR_ATTR = "error";       
    

    // Dependencies
    private final ChildService childService;
    private final WeeklySanctionService weeklySanctionService;
    private final RegleDisciplineService regleDisciplineService;
    private final MessageSource messageSource;
    /**
     * Get the sanctions list page
     */
    @GetMapping
    public String getSanctionsPage(Model model, @AuthenticationPrincipal User user) {
        // Get the children for the current user
        List<ChildDto> children = childService.getChildrenByParentId(user.getId());
        
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
            @AuthenticationPrincipal User user,
            Model model) {
        
        log.debug("Applying sanction rule {} to child {}", ruleCode, childId);
        
        try {
            // Get the rules first so they are available even if there's an error
            var rules = regleDisciplineService.getAllRules();
            model.addAttribute( RULES_ATTR, rules);
            
            // Get the child
            ChildDto child = childService.getChildById(childId);
            model.addAttribute( CHILDREN_ATTR, child);
            
            // Apply the sanction - this may throw an exception
            WeeklySanctionDto weeklySanction = weeklySanctionService.applySanction(childId, ruleCode, user.getId());
            
            // Create a ChildSanctionViewDto
            ChildSanctionViewDto childSanction = new ChildSanctionViewDto(child, weeklySanction);
            
            // Add attributes to the model
            model.addAttribute(CHILD_SANCTION, childSanction);
            
            return "sanctions/fragments/child-points :: points";
        } catch (Exception e) {
            log.error("Error applying sanction rule {} to child {}: {}", ruleCode, childId, e.getMessage(), e);
            
            // Get the child and current sanctions to show current state
            ChildDto child = null;
            WeeklySanctionDto currentSanction = null;
            
            try {
                child = childService.getChildById(childId);
                currentSanction = weeklySanctionService.getCurrentWeekSanction(childId);
            } catch (Exception ex) {
                log.error("Error retrieving child or current sanction: {}", ex.getMessage());
                model.addAttribute(ERROR_ATTR, messageSource.getMessage("sanction.retrieve.error", null, Locale.getDefault()));
                var rules = regleDisciplineService.getAllRules();
                model.addAttribute(RULES_ATTR, rules);
                return "sanctions/fragments/child-points :: points";
            }
            
            var rules = regleDisciplineService.getAllRules();
            
            // Create view DTO with current data
            ChildSanctionViewDto childSanction = new ChildSanctionViewDto(child, currentSanction);
            
            // Add attributes to the model
            model.addAttribute(CHILD_SANCTION, childSanction);
            model.addAttribute(RULES_ATTR, rules);
            model.addAttribute(ERROR_ATTR, messageSource.getMessage("sanction.apply.error", null, Locale.getDefault()));
            
            return "sanctions/fragments/child-points :: points";
        }
    }
    
    /**
     * Apply a manual points adjustment
     */
    @PutMapping("/{childId}/points/{points}")
    public String applySanction(
            @PathVariable String childId,
            @PathVariable Integer points,
            @AuthenticationPrincipal User user,
            Model model) {
        
        log.debug("Applying points {} to child {}", points, childId);
        
        try {
            // Apply the points - treat points value as a rule code
            WeeklySanctionDto weeklySanction = weeklySanctionService.applySanction(childId, String.valueOf(points), user.getId());
            
            // Get the child
            ChildDto child = childService.getChildById(childId);
            
            // Get the rules
            var rules = regleDisciplineService.getAllRules();
            
            // Create a ChildSanctionViewDto
            ChildSanctionViewDto childSanction = new ChildSanctionViewDto(child, weeklySanction);
            
            // Add attributes to the model
            model.addAttribute(CHILD_SANCTION, childSanction);
            model.addAttribute(RULES_ATTR, rules);
            
            return "sanctions/fragments/child-points :: points";
        } catch (Exception e) {
            log.error("Error applying points {} to child {}: {}", points, childId, e.getMessage(), e);
            
            // Get the child and current sanctions to show current state
            ChildDto child = childService.getChildById(childId);
            WeeklySanctionDto currentSanction = weeklySanctionService.getCurrentWeekSanction(childId);
            var rules = regleDisciplineService.getAllRules();
            
            // Create view DTO with current data
            ChildSanctionViewDto childSanction = new ChildSanctionViewDto(child, currentSanction);
            
            // Add attributes to the model
            model.addAttribute(CHILD_SANCTION, childSanction);
            model.addAttribute(RULES_ATTR, rules);
            model.addAttribute( ERROR_ATTR,     messageSource.getMessage("sanction.apply.error", null, Locale.getDefault()));
            
            return "sanctions/fragments/child-points :: points";
        }
    }
} 