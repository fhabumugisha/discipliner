package com.buseni.discipline.sanction.controller;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.buseni.discipline.children.service.ChildService;
import com.buseni.discipline.sanction.service.RegleDisciplineService;
import com.buseni.discipline.sanction.service.SanctionService;
import com.buseni.discipline.users.domain.User;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/sanctions")
@RequiredArgsConstructor
public class SanctionController {

    private final ChildService childService;
    private final SanctionService sanctionService;
    private final RegleDisciplineService regleDisciplineService;

    @GetMapping
    public String getSanctionsPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        var rules = regleDisciplineService.getAllRules();
        model.addAttribute("rules", rules);

        User user = (User) userDetails;
        var children = childService.getChildrenByParentId(user.getId());
        model.addAttribute("children", children);
        return "sanctions/list";
    }

    @PutMapping("/{childId}/rule/{ruleCode}")
    @HxRequest
    public String applySanctionByRule(
            @PathVariable String childId,
            @PathVariable String ruleCode,
            Model model) {
        var updatedChild = sanctionService.applySanctionByRule(childId, ruleCode);
        model.addAttribute("child", updatedChild);
        return "sanctions/fragments/child-points :: points";
    }

    @PutMapping("/{childId}/points/{points}")
    @HxRequest
    public String applySanction(
            @PathVariable String childId,
            @PathVariable int points,
            Model model) {
        var updatedChild = sanctionService.applySanction(childId, -Math.abs(points));
        model.addAttribute("child", updatedChild);
        return "sanctions/fragments/child-points :: points";
    }
} 