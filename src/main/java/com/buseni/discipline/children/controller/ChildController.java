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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller responsible for managing children-related operations and interfaces.
 * Handles CRUD operations for children, and invitation management.
 */
@Controller
@RequestMapping("/children")
@RequiredArgsConstructor
@SessionAttributes({ChildController.CHILDREN_MODEL_ATTRIBUTE, ChildController.PARENT_ID_MODEL_ATTRIBUTE, 
                   ChildController.PENDING_INVITATIONS_MODEL_ATTRIBUTE})
@Slf4j
public class ChildController {

    // Model attribute constants
    public static final String CHILDREN_MODEL_ATTRIBUTE = "children";
    public static final String CHILD_DTO_MODEL_ATTRIBUTE = "childDto";
    public static final String PARENT_ID_MODEL_ATTRIBUTE = "parentId";
    public static final String PENDING_INVITATIONS_MODEL_ATTRIBUTE = "pendingInvitations";
    
    // View fragments constants
    private static final String EDIT_MODE_ATTRIBUTE = "editMode";
    private static final String SUCCESS_MESSAGE_ATTRIBUTE = "successMessage";
    private static final String ERROR_MESSAGE_ATTRIBUTE = "errorMessage";
    private static final String CHILDREN_LIST_FRAGMENT_CHILD_FORM = "children/list :: #childForm";
    private static final String CHILDREN_LIST_FRAGMENT_CHILDREN_CONTAINER = "children/list :: #childrenContainer";
    private static final String CHILDREN_LIST_FRAGMENT_CHILDREN_LIST = "children/list :: #childrenList";
    private static final String CHILDREN_LIST_PAGE = "children/list";
    
    // Notification constants
    private static final String HX_TRIGGER_HEADER = "HX-Trigger";
    private static final String HX_REFRESH_HEADER = "HX-Refresh";
    private static final String NOTIFICATION_FORMAT = "{\"notification\": {\"message\": \"%s\", \"type\": \"%s\"}}";
    private static final String SUCCESS_TYPE = "success";
    private static final String ERROR_TYPE = "error";
    
    // Message keys
    private static final String INVITATION_ACCEPT_SUCCESS_KEY = "children.invitations.accepted.success";
    private static final String INVITATION_REVOKE_SUCCESS_KEY = "children.invitations.revoked.success";
    private static final String DEFAULT_ACCEPT_SUCCESS_MSG = "Invitation accepted successfully";
    private static final String DEFAULT_REVOKE_SUCCESS_MSG = "Invitation revoked successfully";

    // Dependencies
    private final ChildService childService;
    private final ChildInvitationService invitationService;
    private final MessageSource messageSource;

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

    /**
     * Handles the acceptance of an invitation.
     * 
     * @param parentId The ID of the parent accepting the invitation
     * @param invitationId The ID of the invitation being accepted
     * @param model The Spring MVC model
     * @param response The HTTP response for setting headers
     * @return The view name to render
     */
    @PostMapping("/{parentId}/invitations/{invitationId}/accept")
    @HxRequest
    public String acceptInvitation(@PathVariable String parentId,
                                  @PathVariable String invitationId,
                                  Model model,
                                  HttpServletResponse response) {
        try {
            // Accept the invitation
            log.info("Processing invitation acceptance: invitation={}, parent={}", invitationId, parentId);
            invitationService.acceptInvitationById(invitationId);
            
            // Update model with fresh data
            updateModelWithCurrentData(model, parentId);
            
            // Set success notification
            sendNotification(response, INVITATION_ACCEPT_SUCCESS_KEY, DEFAULT_ACCEPT_SUCCESS_MSG, SUCCESS_TYPE);
            
            log.info("Invitation accepted successfully: invitation={}", invitationId);
        } catch (InvalidOperationException e) {
            log.error("Error accepting invitation: {}", e.getMessage(), e);
            
            // Update model with current data
            updateModelWithCurrentData(model, parentId);
            
            // Set error notification
            sendNotification(response, e.getMessageKey(), e.getMessage(), ERROR_TYPE);
        }
        
        // Force page refresh
        response.setHeader(HX_REFRESH_HEADER, "true");
        return CHILDREN_LIST_FRAGMENT_CHILDREN_CONTAINER;
    }

    /**
     * Handles the revocation of an invitation.
     * 
     * @param parentId The ID of the parent revoking the invitation
     * @param invitationId The ID of the invitation being revoked
     * @param model The Spring MVC model
     * @param response The HTTP response for setting headers
     * @return The view name to render
     */
    @PostMapping("/{parentId}/invitations/{invitationId}/revoke")
    @HxRequest
    public String revokeInvitation(@PathVariable String parentId,
                                  @PathVariable String invitationId,
                                  Model model,
                                  HttpServletResponse response) {
        try {
            // Revoke the invitation
            log.info("Processing invitation revocation: invitation={}, parent={}", invitationId, parentId);
            invitationService.revokeInvitationById(invitationId);
            
            // Update model with fresh data
            updateModelWithCurrentData(model, parentId);
            
            // Set success notification
            sendNotification(response, INVITATION_REVOKE_SUCCESS_KEY, DEFAULT_REVOKE_SUCCESS_MSG, SUCCESS_TYPE);
            
            log.info("Invitation revoked successfully: invitation={}", invitationId);
        } catch (InvalidOperationException e) {
            log.error("Error revoking invitation: {}", e.getMessage(), e);
            
            // Update model with current data
            updateModelWithCurrentData(model, parentId);
            
            // Set error notification
            sendNotification(response, e.getMessageKey(), e.getMessage(), ERROR_TYPE);
        }
        
        // Force page refresh
        response.setHeader(HX_REFRESH_HEADER, "true");
        return CHILDREN_LIST_FRAGMENT_CHILDREN_CONTAINER;
    }
    
    /**
     * Updates the model with the current children and invitation data.
     * 
     * @param model The Spring MVC model
     * @param parentId The parent ID
     */
    private void updateModelWithCurrentData(Model model, String parentId) {
        // Get current data
        List<ChildDto> children = childService.getChildrenByParentId(parentId);
        List<ChildPendingInvitationDto> invitations = invitationService.getPendingInvitations(parentId);
        
        // Update model
        model.addAttribute(CHILDREN_MODEL_ATTRIBUTE, children);
        model.addAttribute(PENDING_INVITATIONS_MODEL_ATTRIBUTE, invitations);
        model.addAttribute(CHILD_DTO_MODEL_ATTRIBUTE, new ChildDto("", "", Integer.valueOf(0), Integer.valueOf(0)));
        model.addAttribute(PARENT_ID_MODEL_ATTRIBUTE, parentId);
    }
    
    /**
     * Sends a notification using the HTMX notification system.
     * 
     * @param response The HTTP response for setting headers
     * @param messageKey The message key for internationalization
     * @param defaultMessage The default message if the key is not found
     * @param notificationType The type of notification (success, error, info)
     */
    private void sendNotification(HttpServletResponse response, String messageKey, String defaultMessage, String notificationType) {
        String message = messageSource.getMessage(messageKey, null, defaultMessage, Locale.getDefault());
        String triggerJson = String.format(NOTIFICATION_FORMAT, message, notificationType);
        response.setHeader(HX_TRIGGER_HEADER, triggerJson);
    }
} 