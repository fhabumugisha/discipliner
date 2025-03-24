package com.buseni.discipline.children.controller;

import java.util.List;
import java.util.UUID;

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

import com.buseni.discipline.children.dto.ChildDto;
import com.buseni.discipline.children.dto.ChildInvitationDto;
import com.buseni.discipline.children.dto.ChildPendingInvitationDto;
import com.buseni.discipline.children.service.ChildInvitationService;
import com.buseni.discipline.children.service.ChildService;
import com.buseni.discipline.users.domain.User;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/children")
@RequiredArgsConstructor
public class ChildController {

    private static final String CHILDREN_MODEL_ATTRIBUTE = "children";
    private static final String CHILD_DTO_MODEL_ATTRIBUTE = "childDto";
    private static final String PARENT_ID_MODEL_ATTRIBUTE = "parentId";
    private static final String EDIT_MODE_ATTRIBUTE = "editMode";
    private final ChildService childService;
    private final ChildInvitationService invitationService;
    private static final String CHILDREN_LIST_FRAGMENT_CHILD_FORM = "children/list :: #childForm";
    private static final String CHILDREN_LIST_FRAGMENT_CHILDREN_CONTAINER = "children/list :: #childrenContainer";
    private static final String CHILDREN_LIST_FRAGMENT_CHILDREN_LIST = "children/list :: #childrenList";    
    private static final String CHILDREN_LIST_PAGE = "children/list";
    private static final String PENDING_INVITATIONS_MODEL_ATTRIBUTE = "pendingInvitations";


    @GetMapping
    public String getChildrenPage(Model model, Authentication authentication) {
        // We'll get the parent ID from the authenticated user in a real scenario
        User userDetails = (User) authentication.getPrincipal();
        String parentId = userDetails.getId();
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
        model.addAttribute(PARENT_ID_MODEL_ATTRIBUTE, parentId);
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
            model.addAttribute(CHILDREN_MODEL_ATTRIBUTE, childService.getChildrenByParentId(parentId));
            model.addAttribute(PARENT_ID_MODEL_ATTRIBUTE, parentId);
            return CHILDREN_LIST_FRAGMENT_CHILD_FORM;
        }

        childService.createChild(parentId, childDto);
        model.addAttribute(CHILDREN_MODEL_ATTRIBUTE, childService.getChildrenByParentId(parentId));
        model.addAttribute(CHILD_DTO_MODEL_ATTRIBUTE, new ChildDto("", "", Integer.valueOf(0), Integer.valueOf(0)));
        model.addAttribute(PARENT_ID_MODEL_ATTRIBUTE, parentId);
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
            model.addAttribute(CHILDREN_MODEL_ATTRIBUTE, childService.getChildrenByParentId(parentId));
            model.addAttribute(PARENT_ID_MODEL_ATTRIBUTE, parentId);
            model.addAttribute(EDIT_MODE_ATTRIBUTE, true);
            return CHILDREN_LIST_FRAGMENT_CHILD_FORM;
        }

        childService.updateChild(childId, childDto);
        model.addAttribute(CHILDREN_MODEL_ATTRIBUTE, childService.getChildrenByParentId(parentId));
        model.addAttribute(CHILD_DTO_MODEL_ATTRIBUTE, new ChildDto("", "", Integer.valueOf(0), Integer.valueOf(0)));
        model.addAttribute(PARENT_ID_MODEL_ATTRIBUTE, parentId);
        return CHILDREN_LIST_FRAGMENT_CHILDREN_CONTAINER;
    }


    @PostMapping("/{parentId}/invitations/{invitationId}/accept")
    @HxRequest
    public String acceptInvitation(@PathVariable String parentId,
                                  @PathVariable String invitationId,
                                  Model model) {
        invitationService.acceptInvitationById(invitationId);
        model.addAttribute(PENDING_INVITATIONS_MODEL_ATTRIBUTE, invitationService.getPendingInvitations(parentId));
        return CHILDREN_LIST_FRAGMENT_CHILDREN_CONTAINER;
    }

    @PostMapping("/{parentId}/invitations/{invitationId}/revoke")
    @HxRequest
    public String revokeInvitation(@PathVariable String parentId,
                                  @PathVariable String invitationId,
                                  Model model) {
        invitationService.revokeInvitationById(invitationId);
        model.addAttribute(PENDING_INVITATIONS_MODEL_ATTRIBUTE, invitationService.getPendingInvitations(parentId));
        return CHILDREN_LIST_FRAGMENT_CHILDREN_CONTAINER;
    }
} 