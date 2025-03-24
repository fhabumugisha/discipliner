package com.buseni.discipline.children.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buseni.discipline.children.domain.Child;
import com.buseni.discipline.children.domain.ChildInvitation;
import com.buseni.discipline.children.domain.ChildInvitation.InvitationStatus;
import com.buseni.discipline.children.dto.ChildInvitationRequest;
import com.buseni.discipline.children.repository.ChildInvitationRepository;
import com.buseni.discipline.children.repository.ChildRepository;
import com.buseni.discipline.children.service.ChildInvitationService;
import com.buseni.discipline.children.service.NotificationService;
import com.buseni.discipline.users.domain.User;
import com.buseni.discipline.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChildInvitationServiceImpl implements ChildInvitationService {

    private final ChildInvitationRepository invitationRepository;
    private final ChildRepository childRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ChildInvitation createInvitation(ChildInvitationRequest request) {
        Child child = childRepository.findById(request.childId())
            .orElseThrow(() -> new IllegalArgumentException("Child not found"));

        // Check if child already has maximum number of parents
        if (child.getParentIds().size() >= 4) {
            throw new IllegalStateException("Child already has maximum number of parents");
        }

        // Check if user already exists
        if (request.email() != null) {
            userRepository.findByEmail(request.email()).ifPresent(user -> {
                if (child.getParentIds().contains(user.getId())) {
                    throw new IllegalStateException("User is already a parent of this child");
                }
            });
        }

        // Create invitation
        ChildInvitation invitation = ChildInvitation.builder()
            .childId(child.getId())
            .inviterId(child.getParentId())
            .inviteeEmail(request.email())
            .inviteePhone(request.phone())
            .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
            .build();

        invitation = invitationRepository.save(invitation);

        // Send notification
        if (request.email() != null) {
            notificationService.sendInvitationEmail(invitation);
        }
        if (request.phone() != null) {
            notificationService.sendInvitationSms(invitation);
        }

        return invitation;
    }

    @Override
    @Transactional
    public ChildInvitation acceptInvitation(String token) {
        ChildInvitation invitation = invitationRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Invitation is no longer valid");
        }

        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new IllegalStateException("Invitation has expired");
        }

        Child child = childRepository.findById(invitation.getChildId())
            .orElseThrow(() -> new IllegalStateException("Child not found"));

        if (child.getParentIds().size() >= 4) {
            throw new IllegalStateException("Child already has maximum number of parents");
        }

        User invitee = userRepository.findByEmail(invitation.getInviteeEmail())
            .orElseThrow(() -> new IllegalStateException("User not found"));

        child.getParentIds().add(invitee.getId());
        childRepository.save(child);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(Instant.now());
        return invitationRepository.save(invitation);
    }

    @Override
    @Transactional
    public void revokeInvitation(String invitationId) {
        ChildInvitation invitation = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Invitation cannot be revoked");
        }

        invitation.setStatus(InvitationStatus.REVOKED);
        invitation.setRevokedAt(Instant.now());
        invitationRepository.save(invitation);
    }

    @Override
    @Scheduled(cron = "0 0 * * * *") // Run every hour
    @Transactional
    public void cleanupExpiredInvitations() {
        List<ChildInvitation> expiredInvitations = invitationRepository
            .findByExpiresAtBeforeAndStatus(Instant.now(), InvitationStatus.PENDING);

        expiredInvitations.forEach(invitation -> {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
        });
    }
} 