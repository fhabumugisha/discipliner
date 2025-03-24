package com.buseni.discipline.children.service;

import com.buseni.discipline.children.domain.ChildInvitation;
import com.buseni.discipline.children.dto.ChildInvitationRequest;

public interface ChildInvitationService {
    /**
     * Create a new child invitation
     * @param request the request containing the invitation details
     * @return the created child invitation
     */
    ChildInvitation createInvitation(ChildInvitationRequest request);
    
    /**
     * Accept a child invitation
     * @param token the token used to accept the invitation
     * @return the accepted child invitation
     */
    ChildInvitation acceptInvitation(String token);
    /**
     * 
     * @param invitationId
     */    
    void revokeInvitation(String invitationId);
    /**
     * Clean up expired invitations
     */
    void cleanupExpiredInvitations();
} 