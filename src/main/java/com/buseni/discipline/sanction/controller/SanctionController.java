package com.buseni.discipline.sanction.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.buseni.discipline.children.dto.ChildDto;
import com.buseni.discipline.children.service.ChildService;
import com.buseni.discipline.sanction.dto.WeeklySanctionDto;
import com.buseni.discipline.sanction.service.RegleDisciplineService;
import com.buseni.discipline.sanction.service.SanctionService;
import com.buseni.discipline.sanction.service.WeeklySanctionService;
import com.buseni.discipline.users.domain.User;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/sanctions")
@RequiredArgsConstructor
@Slf4j
public class SanctionController {

    private final ChildService childService;
    private final SanctionService sanctionService;
    private final RegleDisciplineService regleDisciplineService;
    private final WeeklySanctionService weeklySanctionService;

    @GetMapping
    public String getSanctionsPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        var rules = regleDisciplineService.getAllRules();
        model.addAttribute("rules", rules);

        User user = (User) userDetails;
        model.addAttribute("connectedUserName", user.getUsername());
        
        List<ChildDto> children = childService.getChildrenByParentId(user.getId());
        
        if (!children.isEmpty()) {
            // Get weekly sanctions for each child
            List<WeeklySanctionDto> weeklySanctions = children.stream()
                .map(child -> {
                    try {
                        return weeklySanctionService.getCurrentWeekSanction(child.getId());
                    } catch (Exception e) {
                        log.error("Error getting weekly sanction for child {}: {}", child.getId(), e.getMessage());
                        try {
                            // Initialize weekly points for this child
                            weeklySanctionService.initializeWeeklyPoints();
                            return weeklySanctionService.getCurrentWeekSanction(child.getId());
                        } catch (Exception ex) {
                            log.error("Failed to initialize weekly points for child {}: {}", child.getId(), ex.getMessage());
                            return null;
                        }
                    }
                })
                .filter(sanction -> sanction != null)
                .collect(Collectors.toList());
            
            model.addAttribute("weeklySanctions", weeklySanctions);
        }
        
        model.addAttribute("children", children);
        return "sanctions/list";
    }

    @PutMapping("/{childId}/rule/{ruleCode}")
    @HxRequest
    public String applySanctionByRule(
            @PathVariable String childId,
            @PathVariable String ruleCode,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = (User) userDetails;
            var updatedSanction = weeklySanctionService.applySanction(childId, ruleCode, user.getId());
            model.addAttribute("weeklySanction", updatedSanction);
            model.addAttribute("child", childService.getChildById(childId));
            model.addAttribute("rules", regleDisciplineService.getAllRules());
            return "sanctions/fragments/child-points :: points";
        } catch (Exception e) {
            log.error("Error applying sanction for child {} with rule {}: {}", childId, ruleCode, e.getMessage());
            // Try to initialize weekly points and retry
            try {
                weeklySanctionService.initializeWeeklyPoints();
                var updatedSanction = weeklySanctionService.applySanction(childId, ruleCode, ((User) userDetails).getId());
                model.addAttribute("weeklySanction", updatedSanction);
                model.addAttribute("child", childService.getChildById(childId));
                model.addAttribute("rules", regleDisciplineService.getAllRules());
                return "sanctions/fragments/child-points :: points";
            } catch (Exception ex) {
                log.error("Failed to apply sanction after initialization: {}", ex.getMessage());
                throw ex;
            }
        }
    }

    @PutMapping("/{childId}/points/{points}")
    @HxRequest
    public String applySanction(
            @PathVariable String childId,
            @PathVariable int points,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = (User) userDetails;
            var updatedSanction = weeklySanctionService.applySanction(childId, String.valueOf(points), user.getId());
            model.addAttribute("weeklySanction", updatedSanction);
            return "sanctions/fragments/child-points :: points";
        } catch (Exception e) {
            log.error("Error applying points sanction for child {}: {}", childId, e.getMessage());
            throw e;
        }
    }
} 