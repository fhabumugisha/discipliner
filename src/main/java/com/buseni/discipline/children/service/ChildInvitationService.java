package com.buseni.discipline.children.service;

import java.util.List;

import com.buseni.discipline.children.dto.ChildInvitationDto;
import com.buseni.discipline.children.dto.ChildInvitationRequest;
import com.buseni.discipline.children.dto.ChildPendingInvitationDto;

public interface ChildInvitationService {
    /**
     * Create a new child invitation
     * @param request the request containing the invitation details
     * @return the created child invitation
     */
    void createInvitation(ChildInvitationRequest request);
    
    /**
     * Accept a child invitation
     * @param token the token used to accept the invitation
     * @return the accepted child invitation
     */
    void acceptInvitation(String token);
    /**
     * 
     * @param invitationId
     */    
    void revokeInvitation(String invitationId);
    /**
     * Clean up expired invitations
     */
    void cleanupExpiredInvitations();
    /**
     * Get pending invitations for a child
     * @param childId the id of the parent
     * @return the pending invitations
     */
    List<ChildPendingInvitationDto> getPendingInvitations(String parentId);

    /**
     * Accept a child invitation
     * @param invitationId the id of the invitation
     */
    void acceptInvitationById(String invitationId);

    /**
     * Revoke a child invitation
     * @param invitationId the id of the invitation
     */
    void revokeInvitationById(String invitationId);
} 