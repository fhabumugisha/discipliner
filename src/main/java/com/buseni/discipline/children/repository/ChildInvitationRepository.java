package com.buseni.discipline.children.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.buseni.discipline.children.domain.ChildInvitation;
import com.buseni.discipline.children.domain.ChildInvitation.InvitationStatus;

public interface ChildInvitationRepository extends MongoRepository<ChildInvitation, String> {
    
    Optional<ChildInvitation> findByToken(String token);
    
    List<ChildInvitation> findByChildIdAndStatus(String childId, InvitationStatus status);
    
    List<ChildInvitation> findByInviteeEmailAndStatus(String email, InvitationStatus status);
    
    List<ChildInvitation> findByInviteePhoneAndStatus(String phone, InvitationStatus status);
    
    List<ChildInvitation> findByExpiresAtBeforeAndStatus(Instant now, InvitationStatus status);

    List<ChildInvitation> findByInviteePhoneOrInviteeEmailAndStatus(String phone, String email,
            InvitationStatus status);
} 