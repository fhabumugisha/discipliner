package com.buseni.discipline.children.controller;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.buseni.discipline.children.dto.ChildInvitationRequest;
import com.buseni.discipline.children.service.ChildInvitationService;
import com.buseni.discipline.children.service.ChildService;
import com.buseni.discipline.common.exception.InvalidOperationException;
import com.buseni.discipline.common.exception.ResourceNotFoundException;
import com.buseni.discipline.common.exception.ValidationException;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
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
    private static final String INVITATION_REQUEST_ATTR = "invitationRequest";


    @GetMapping("/form")
    @HxRequest
    public String getInvitationForm(@RequestParam String childId, Model model) {
        log.debug("Loading invitation form for child: {}", childId);
        
        try {
            // Verify if the child exists
            childService.getChildById(childId);
            
            // Create empty request object for the form
            model.addAttribute(INVITATION_REQUEST_ATTR, new ChildInvitationRequest(childId, null, null));
            return "fragments/invitation-form :: form";
        } catch (ResourceNotFoundException e) {
            log.warn("Child not found: {}", childId);
            model.addAttribute("success", false);
            model.addAttribute(ERROR_ATTR, messageSource.getMessage("child.not.found", null, "Child not found", Locale.getDefault()));
            return "fragments/invitation-form :: form";
        }
    }

    @PostMapping("/create")
    @HxRequest
    @ResponseStatus(HttpStatus.OK)
    public String createInvitation(ChildInvitationRequest request, 
                                 BindingResult bindingResult,
                                 Model model) {
        log.debug("Creating invitation for child: {}", request.childId());
        
        try {
            // Create the invitation with validation in the service
            invitationService.createInvitation(request);
            
            // Set success response
            model.addAttribute("success", true);
            model.addAttribute(MESSAGE_ATTR, messageSource.getMessage("invitation.sent.success", null, "Invitation sent successfully", Locale.getDefault()));
            
            model.addAttribute(INVITATION_REQUEST_ATTR, request);

            // Return the form with success message
            return "fragments/invitation-form :: form";
        } catch (ValidationException e) {
            log.warn("Validation error creating invitation: {}", e.getMessage());
                model.addAttribute(INVITATION_REQUEST_ATTR, request);
            model.addAttribute("success", false);
            model.addAttribute(ERROR_ATTR, messageSource.getMessage(e.getMessage(), null, e.getMessage(), Locale.getDefault()));
            return "fragments/invitation-form :: form";
        } catch (ResourceNotFoundException e) {
            log.warn("Resource not found creating invitation: {}", e.getMessage());
            model.addAttribute(INVITATION_REQUEST_ATTR, request);
            model.addAttribute("success", false);
            model.addAttribute(ERROR_ATTR, messageSource.getMessage("child.not.found", null, "Child not found", Locale.getDefault()));
            return "fragments/invitation-form :: form";
        } catch (InvalidOperationException e) {
            log.warn("Invalid operation creating invitation: {}", e.getMessage());
            model.addAttribute(INVITATION_REQUEST_ATTR, request);
            model.addAttribute("success", false);
            model.addAttribute(ERROR_ATTR, messageSource.getMessage(e.getMessage(), null, "Error sending invitation", Locale.getDefault()));
            return "fragments/invitation-form :: form";
        } catch (Exception e) {
            log.error("Unexpected error creating invitation", e);
            model.addAttribute(INVITATION_REQUEST_ATTR, request);
            model.addAttribute("success", false);
            model.addAttribute(ERROR_ATTR, messageSource.getMessage("invitation.error", null, "An unexpected error occurred", Locale.getDefault()));
            return "fragments/invitation-form :: form";
        }
    }

    @GetMapping("/accept")
    public String acceptInvitation(@RequestParam String token, Model model) {
        try {
            invitationService.acceptInvitation(token);
            model.addAttribute("success", true);
            model.addAttribute(MESSAGE_ATTR, messageSource.getMessage("invitation.accepted.success", null, "Invitation accepted successfully", Locale.getDefault()));
        } catch (ResourceNotFoundException | InvalidOperationException e) {
            model.addAttribute("success", false);
            model.addAttribute(ERROR_ATTR, messageSource.getMessage(e.getMessage(), null, "Error accepting invitation", Locale.getDefault()));
        }

        return "invitation-result";
    }

    @PostMapping("/revoke")
    @HxRequest
    public String revokeInvitation(@RequestParam String invitationId, Model model) {
        try {
            invitationService.revokeInvitation(invitationId);
            model.addAttribute("success", true);
            model.addAttribute(MESSAGE_ATTR, messageSource.getMessage("invitation.revoked.success", null, "Invitation revoked successfully", Locale.getDefault()));
      
            return "fragments/invitation-result :: success";
        } catch (ResourceNotFoundException | InvalidOperationException e) {
            model.addAttribute("success", false);
            model.addAttribute(ERROR_ATTR, messageSource.getMessage(e.getMessage(), null, "Error revoking invitation", Locale.getDefault()));
            return "fragments/invitation-result :: error";
        }
    }
} 