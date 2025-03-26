package com.buseni.discipline.children.controller;

import java.util.List;
import java.util.Locale;


import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import com.buseni.discipline.children.dto.ChildDto;

import com.buseni.discipline.children.dto.ChildPendingInvitationDto;
import com.buseni.discipline.children.service.ChildInvitationService;
import com.buseni.discipline.children.service.ChildService;
import com.buseni.discipline.common.exception.InvalidOperationException;
import com.buseni.discipline.users.domain.User;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Controller
@RequestMapping("/children")
@RequiredArgsConstructor
@SessionAttributes({ChildController.CHILDREN_MODEL_ATTRIBUTE, ChildController.PARENT_ID_MODEL_ATTRIBUTE, 
                   ChildController.PENDING_INVITATIONS_MODEL_ATTRIBUTE})
@Slf4j
public class ChildController {

    public static final String CHILDREN_MODEL_ATTRIBUTE = "children";
    public static final String CHILD_DTO_MODEL_ATTRIBUTE = "childDto";
    public static final String PARENT_ID_MODEL_ATTRIBUTE = "parentId";
    private static final String EDIT_MODE_ATTRIBUTE = "editMode";
    private final ChildService childService;
    private final ChildInvitationService invitationService;
    private final MessageSource messageSource;      
    private static final String CHILDREN_LIST_FRAGMENT_CHILD_FORM = "children/list :: #childForm";
    private static final String CHILDREN_LIST_FRAGMENT_CHILDREN_CONTAINER = "children/list :: #childrenContainer";
    private static final String CHILDREN_LIST_FRAGMENT_CHILDREN_LIST = "children/list :: #childrenList";    
    private static final String CHILDREN_LIST_PAGE = "children/list";
    public static final String PENDING_INVITATIONS_MODEL_ATTRIBUTE = "pendingInvitations";
    private static final String SUCCESS_MESSAGE_ATTRIBUTE = "successMessage";


    @GetMapping
    public String getChildrenPage(Model model, Authentication authentication, SessionStatus sessionStatus) {
        // Reset session at initial page load to avoid stale data
        sessionStatus.setComplete();
        
        // Get parent ID from authenticated user
        User userDetails = (User) authentication.getPrincipal();
        String parentId = userDetails.getId();
        
        // Load fresh data for initial page load
        List<ChildDto> children = childService.getChildrenByParentId(parentId);
        List<ChildPendingInvitationDto> pendingInvitations = invitationService.getPendingInvitations(parentId);
        
        model.addAttribute(PENDING_INVITATIONS_MODEL_ATTRIBUTE, pendingInvitations);
        model.addAttribute(CHILDREN_MODEL_ATTRIBUTE, children);
        model.addAttribute(CHILD_DTO_MODEL_ATTRIBUTE, new ChildDto("", "", Integer.valueOf(0), Integer.valueOf(0)));
        model.addAttribute(PARENT_ID_MODEL_ATTRIBUTE, parentId);
        return CHILDREN_LIST_PAGE;
    }

    @GetMapping("/{parentId}/{childId}/edit")
    @HxRequest
    public String getEditForm(@PathVariable String parentId,
                            @PathVariable String childId,
                            Model model) {
        ChildDto childDto = childService.getChildById(childId);
        model.addAttribute(CHILD_DTO_MODEL_ATTRIBUTE, childDto);
        model.addAttribute(EDIT_MODE_ATTRIBUTE, true);
        return CHILDREN_LIST_FRAGMENT_CHILD_FORM;
    }

    @PostMapping("/{parentId}")
    @HxRequest
    public String addChild(@PathVariable String parentId,
                         @Valid @ModelAttribute(CHILD_DTO_MODEL_ATTRIBUTE) ChildDto childDto,
                         BindingResult bindingResult,
                         Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute(EDIT_MODE_ATTRIBUTE, false);
            return CHILDREN_LIST_FRAGMENT_CHILD_FORM;
        }

        // Create the child
        childService.createChild(parentId, childDto);
        
        // Update the session data after creation
        List<ChildDto> updatedChildren = childService.getChildrenByParentId(parentId);
        model.addAttribute(CHILDREN_MODEL_ATTRIBUTE, updatedChildren);
        
        // Make sure we also update the pending invitations
        List<ChildPendingInvitationDto> pendingInvitations = invitationService.getPendingInvitations(parentId);
        model.addAttribute(PENDING_INVITATIONS_MODEL_ATTRIBUTE, pendingInvitations);
        
        model.addAttribute(CHILD_DTO_MODEL_ATTRIBUTE, new ChildDto("", "", Integer.valueOf(0), Integer.valueOf(0)));
        
        // Add success message
        String successMessage = messageSource.getMessage("children.added.success", null, "Child added successfully", Locale.getDefault());
        model.addAttribute(SUCCESS_MESSAGE_ATTRIBUTE, successMessage);
        
        return CHILDREN_LIST_FRAGMENT_CHILDREN_CONTAINER;
    }

    @PutMapping("/{parentId}/{childId}")
    @HxRequest
    public String updateChild(@PathVariable String parentId,
                            @PathVariable String childId,
                            @Valid @ModelAttribute(CHILD_DTO_MODEL_ATTRIBUTE) ChildDto childDto,
                            BindingResult bindingResult,
                            Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute(EDIT_MODE_ATTRIBUTE, true);
            return CHILDREN_LIST_FRAGMENT_CHILD_FORM;
        }

        // Update the child
        childService.updateChild(childId, childDto);
        
        // Update the session data after update
        List<ChildDto> updatedChildren = childService.getChildrenByParentId(parentId);
        model.addAttribute(CHILDREN_MODEL_ATTRIBUTE, updatedChildren);
        
        // Make sure we also update the pending invitations
        List<ChildPendingInvitationDto> pendingInvitations = invitationService.getPendingInvitations(parentId);
        model.addAttribute(PENDING_INVITATIONS_MODEL_ATTRIBUTE, pendingInvitations);
        
        model.addAttribute(CHILD_DTO_MODEL_ATTRIBUTE, new ChildDto("", "", Integer.valueOf(0), Integer.valueOf(0)));
        
        // Add success message
        String successMessage = messageSource.getMessage("children.updated.success", null, "Child updated successfully", Locale.getDefault());
        model.addAttribute(SUCCESS_MESSAGE_ATTRIBUTE, successMessage);
        
        return CHILDREN_LIST_FRAGMENT_CHILDREN_CONTAINER;
    }


    @PostMapping("/{parentId}/invitations/{invitationId}/accept")
    @HxRequest
    public String acceptInvitation(@PathVariable String parentId,
                                  @PathVariable String invitationId,
                                  Model model) {
        try {
            // Accept the invitation
            invitationService.acceptInvitationById(invitationId);
            
            // Update both children list and pending invitations
            List<ChildDto> updatedChildren = childService.getChildrenByParentId(parentId);
            List<ChildPendingInvitationDto> updatedInvitations = invitationService.getPendingInvitations(parentId);
            
            model.addAttribute(CHILDREN_MODEL_ATTRIBUTE, updatedChildren);
            model.addAttribute(PENDING_INVITATIONS_MODEL_ATTRIBUTE, updatedInvitations);
            model.addAttribute(CHILD_DTO_MODEL_ATTRIBUTE, new ChildDto("", "", Integer.valueOf(0), Integer.valueOf(0)));
            model.addAttribute(PARENT_ID_MODEL_ATTRIBUTE, parentId);
            
            // Add a success message
            String successMessage = messageSource.getMessage("children.invitations.accepted.success", null, "Invitation accepted successfully", Locale.getDefault());
            model.addAttribute(SUCCESS_MESSAGE_ATTRIBUTE, successMessage);
        } catch (InvalidOperationException e) {
            // Log the error (you can use System.out.println if you don't have a logger configured)
            log.error("Error accepting invitation: {}", e.getMessage());
            
            // Update model with current state
            model.addAttribute(CHILDREN_MODEL_ATTRIBUTE, childService.getChildrenByParentId(parentId));
            model.addAttribute(PENDING_INVITATIONS_MODEL_ATTRIBUTE, invitationService.getPendingInvitations(parentId));
            model.addAttribute(CHILD_DTO_MODEL_ATTRIBUTE, new ChildDto("", "", Integer.valueOf(0), Integer.valueOf(0)));
            model.addAttribute(PARENT_ID_MODEL_ATTRIBUTE, parentId);
            
            // Add error message as a normal success message (since we don't have error message in the template)
            String errorMessage = messageSource.getMessage("children.invitations.error", null, "Failed to accept invitation. Please try again.", Locale.getDefault());
            model.addAttribute(SUCCESS_MESSAGE_ATTRIBUTE, errorMessage);
        }
        
        return CHILDREN_LIST_FRAGMENT_CHILDREN_CONTAINER;
    }

    @PostMapping("/{parentId}/invitations/{invitationId}/revoke")
    @HxRequest
    public String revokeInvitation(@PathVariable String parentId,
                                  @PathVariable String invitationId,
                                  Model model) {
        try {
            // Revoke the invitation
            invitationService.revokeInvitationById(invitationId);
            
        // Always update both children list and pending invitations to ensure consistency
        List<ChildDto> updatedChildren = childService.getChildrenByParentId(parentId);
        List<ChildPendingInvitationDto> updatedInvitations = invitationService.getPendingInvitations(parentId);
        
        model.addAttribute(CHILDREN_MODEL_ATTRIBUTE, updatedChildren);
        model.addAttribute(PENDING_INVITATIONS_MODEL_ATTRIBUTE, updatedInvitations);
        model.addAttribute(CHILD_DTO_MODEL_ATTRIBUTE, new ChildDto("", "", Integer.valueOf(0), Integer.valueOf(0)));
        model.addAttribute(PARENT_ID_MODEL_ATTRIBUTE, parentId);
        
        // Add a success message
        String successMessage = messageSource.getMessage("children.invitations.revoked.success", null, "Invitation revoked successfully", Locale.getDefault());
        model.addAttribute(SUCCESS_MESSAGE_ATTRIBUTE, successMessage);
            
        return CHILDREN_LIST_FRAGMENT_CHILDREN_CONTAINER;
        } catch (InvalidOperationException e) {
            log.error("Error revoking invitation: {}", e.getMessage());
            return CHILDREN_LIST_FRAGMENT_CHILDREN_CONTAINER;
        }
    }
} 