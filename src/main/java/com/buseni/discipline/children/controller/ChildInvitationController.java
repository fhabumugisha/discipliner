package com.buseni.discipline.children.controller;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.buseni.discipline.children.dto.ChildInvitationRequest;
import com.buseni.discipline.children.service.ChildInvitationService;
import com.buseni.discipline.children.service.ChildService;
import com.buseni.discipline.common.exception.InvalidOperationException;
import com.buseni.discipline.common.exception.ResourceNotFoundException;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/children/invitations")
@RequiredArgsConstructor
public class ChildInvitationController {

    private final ChildInvitationService invitationService;
    private final ChildService childService;
    private final MessageSource messageSource;
    private static final String MESSAGE_ATTR = "message";
    private static final String ERROR_ATTR = "error";   

    @GetMapping("/form")
    @HxRequest
    public String getInvitationForm(@RequestParam String childId, Model model) {
        log.debug("Loading invitation form for child: {}", childId);
        
        // Vérifier si l'enfant existe
        childService.getChildById(childId);
        
        // Créer un nouvel objet DTO vide pour le formulaire
        model.addAttribute("invitationRequest", new ChildInvitationRequest(childId, null, null));
        model.addAttribute("childId", childId);
        
        return "fragments/invitation-form :: form";
    }

    @PostMapping("/create")
    @HxRequest
    public String createInvitation(@Valid ChildInvitationRequest request, 
                                 BindingResult bindingResult,
                                 Model model) {
        log.debug("Creating invitation for child: {}", request.childId());
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("invitationRequest", request);
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "fragments/invitation-form :: form";
        }

        try {
            invitationService.createInvitation(request);
            model.addAttribute("success", true);
            model.addAttribute(MESSAGE_ATTR, messageSource.getMessage("invitation.sent.success", null, Locale.getDefault()));
        } catch (ResourceNotFoundException  | InvalidOperationException e) {
            model.addAttribute( ERROR_ATTR, messageSource.getMessage(e.getMessage(), null, Locale.getDefault()));
        }

        return "fragments/invitation-result :: result";
    }

    @GetMapping("/accept")
    public String acceptInvitation(@RequestParam String token, Model model) {
        try {
            invitationService.acceptInvitation(token);
            model.addAttribute("success", true);
            model.addAttribute(MESSAGE_ATTR, messageSource.getMessage("invitation.accepted.success", null, Locale.getDefault()));
        } catch (ResourceNotFoundException  | InvalidOperationException e) {
            model.addAttribute( ERROR_ATTR, messageSource.getMessage(e.getMessage(), null, Locale.getDefault()));
        }

        return "invitation-result";
    }

    @PostMapping("/revoke")
    @HxRequest
    public String revokeInvitation(@RequestParam String invitationId, Model model) {
        try {
            invitationService.revokeInvitation(invitationId);
            model.addAttribute("success", true);
            model.addAttribute(MESSAGE_ATTR, messageSource.getMessage("invitation.revoked.success", null, Locale.getDefault()));
      
            return "fragments/invitation-result :: success";
        } catch (ResourceNotFoundException  | InvalidOperationException e) {
            model.addAttribute( ERROR_ATTR, messageSource.getMessage(e.getMessage(), null, Locale.getDefault()));
            return "fragments/invitation-result :: error";
        }
    }
} 