package com.buseni.discipline.children.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buseni.discipline.children.domain.Child;
import com.buseni.discipline.children.domain.ChildInvitation;
import com.buseni.discipline.children.domain.ChildInvitation.InvitationStatus;
import com.buseni.discipline.children.dto.ChildInvitationDto;
import com.buseni.discipline.children.dto.ChildInvitationRequest;
import com.buseni.discipline.children.dto.ChildPendingInvitationDto;
import com.buseni.discipline.children.repository.ChildInvitationRepository;
import com.buseni.discipline.children.repository.ChildRepository;
import com.buseni.discipline.children.service.ChildInvitationService;
import com.buseni.discipline.children.service.EmailService;
import com.buseni.discipline.children.service.SmsService;
import com.buseni.discipline.common.exception.InvalidOperationException;
import com.buseni.discipline.common.exception.ResourceNotFoundException;
import com.buseni.discipline.common.exception.ValidationException;
import com.buseni.discipline.users.domain.User;
import com.buseni.discipline.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChildInvitationServiceImpl implements ChildInvitationService {

    private static final String ERROR_INVITATION_LIMIT_REACHED = "error.invitation.limit.reached";      
    private static final String ERROR_CHILD_NOT_FOUND = "error.child.not.found";
    private static final String ERROR_INVITATION_NOT_FOUND = "error.invitation.not.found";
    private static final String ERROR_USER_NOT_FOUND = "error.user.not.found";
    private static final String ERROR_INVITATION_CANNOT_BE_REVOKED = "error.invitation.cannot.be.revoked";
    private static final String ERROR_INVITATION_ALREADY_PARENT = "error.invitation.already.parent";
    private static final String ERROR_INVITATION_EXPIRED = "error.invitation.expired";
    private static final String ERROR_PARENT_NOT_FOUND = "error.parent.not.found";    
    private static final String ERROR_INVITATION_CANNOT_BE_ACCEPTED = "error.invitation.cannot.be.accepted";

    private final ChildInvitationRepository invitationRepository;
    private final ChildRepository childRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    private static final int MAX_PARENTS = 4;
    private static final int INVITATION_EXPIRY_HOURS = 24;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Override
    @Transactional
    public void createInvitation(ChildInvitationRequest request) {
        log.debug("Creating invitation for child: {} with request: {}", request.childId(), request);

        // Validate request
        validateInvitationRequest(request);

        // Verify child exists
        Child child = childRepository.findById(request.childId())
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_CHILD_NOT_FOUND, request.childId()));

        // Check number of parents
        if (child.getParentIds().size() >= MAX_PARENTS) {
            throw new InvalidOperationException(ERROR_INVITATION_LIMIT_REACHED);
        }

        String token = UUID.randomUUID().toString();
        ChildInvitation invitation = ChildInvitation.builder()
                .childId(child.getId())
                .childName(child.getName())
                .token(token)
                .inviteeEmail(request.email())
                .inviteePhone(request.phone())
                .expiresAt(Instant.now().plus(INVITATION_EXPIRY_HOURS, ChronoUnit.HOURS))
                .build();

        invitationRepository.save(invitation);
        String invitationUrl = buildInvitationUrl(token);

        if (request.email() != null && !request.email().isEmpty()) {
            Optional<User> existingUser = userRepository.findByEmail(request.email());
            if (existingUser.isPresent()) {
                emailService.sendInvitation(request.email(), child.getName(), invitationUrl);
            } else {
                emailService.sendInvitationWithRegistration(request.email(), child.getName(), invitationUrl);
            }
        } else if (request.phone() != null && !request.phone().isEmpty()) {
            Optional<User> existingUser = userRepository.findByPhone(request.phone());
            if (existingUser.isPresent()) {
                smsService.sendInvitation(request.phone(), child.getName(), invitationUrl);
            } else {
                smsService.sendInvitationWithRegistration(request.phone(), child.getName(), invitationUrl);
            }
        }

        log.info("Invitation created successfully for child: {} with token: {}", child.getId(), token);
    }

    /**
     * Validates the invitation request
     * 
     * @param request the invitation request to validate
     * @throws ValidationException if validation fails
     */
    private void validateInvitationRequest(ChildInvitationRequest request) {
        // Validate childId
        if (request.childId() == null || request.childId().isBlank()) {
            throw new ValidationException("child.id.not.blank");
        }
        
        // Validate that at least one contact method is provided
        if ((request.email() == null || request.email().isBlank()) && 
            (request.phone() == null || request.phone().isBlank())) {
            throw new ValidationException("invitation.form.contact.required");
        }
        
        // Validate email if provided
        if (request.email() != null && !request.email().isBlank()) {
            // Email regex pattern
            String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
            if (!request.email().matches(emailRegex)) {
                throw new ValidationException("email.invalid");
            }
        }
        
        // Validate phone if provided
        if (request.phone() != null && !request.phone().isBlank()) {
            // Phone regex pattern for international format (E.164)
            String phoneRegex = "^\\+?[1-9]\\d{1,14}$";
            if (!request.phone().matches(phoneRegex)) {
                throw new ValidationException("phone.invalid");
            }
        }
    }

    @Override
    @Transactional
    public void acceptInvitation(String token) {
        ChildInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_INVITATION_NOT_FOUND));

        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidOperationException(ERROR_INVITATION_EXPIRED);
        }

        Child child = childRepository.findById(invitation.getChildId())
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_CHILD_NOT_FOUND, invitation.getChildId()));

        if (child.getParentIds().size() >= MAX_PARENTS) {
            throw new InvalidOperationException(ERROR_INVITATION_LIMIT_REACHED);
        }

        User user = findUserByEmailOrPhone(invitation.getInviteeEmail(), invitation.getInviteePhone())
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_USER_NOT_FOUND));

        if (child.getParentIds().contains(user.getId())) {
            throw new InvalidOperationException(ERROR_INVITATION_ALREADY_PARENT);
        }

        child.getParentIds().add(user.getId());
        childRepository.save(child);

        invitation.setAcceptedAt(Instant.now());
        invitationRepository.save(invitation);

        log.info("Invitation accepted for child: {} by user: {}", child.getId(), user.getId());
    }

    @Override
    @Transactional
    public void revokeInvitation(String invitationId) {
        ChildInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_INVITATION_NOT_FOUND));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InvalidOperationException(ERROR_INVITATION_CANNOT_BE_REVOKED);
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

    private String buildInvitationUrl(String token) {
        return appBaseUrl + "/children/invitations/accept?token=" + token;
    }

    private Optional<User> findUserByEmailOrPhone(String email, String phone) {
        if (email != null) {
            return userRepository.findByEmail(email);
        } else if (phone != null) {
            return userRepository.findByPhone(phone);
        }
        return Optional.empty();
    }

    @Override
    public List<ChildPendingInvitationDto> getPendingInvitations(String parentId) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException( ERROR_PARENT_NOT_FOUND, parentId));
        List<ChildInvitation> invitations = invitationRepository.findByInviteePhoneOrInviteeEmailAndStatus(
                parent.getPhone(), parent.getEmail(), InvitationStatus.PENDING);
        return invitations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ChildPendingInvitationDto convertToDto(ChildInvitation invitation) {
        return new ChildPendingInvitationDto(
                invitation.getId(),
                invitation.getChildName(),
                invitation.getStatus(),
                invitation.getCreatedAt(),
                invitation.getExpiresAt());
    }

    @Override
    @Transactional
    public void acceptInvitationById(String invitationId) {
        ChildInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException(   ERROR_INVITATION_NOT_FOUND));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InvalidOperationException(ERROR_INVITATION_CANNOT_BE_ACCEPTED);
        }

        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidOperationException(ERROR_INVITATION_EXPIRED);
        }

        Child child = childRepository.findById(invitation.getChildId())
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_CHILD_NOT_FOUND, invitation.getChildId()));

        if (child.getParentIds().size() >= MAX_PARENTS) {
            throw new InvalidOperationException(ERROR_INVITATION_LIMIT_REACHED);
        }

        User user = findUserByEmailOrPhone(invitation.getInviteeEmail(), invitation.getInviteePhone())
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_USER_NOT_FOUND));

        if (child.getParentIds().contains(user.getId())) {
            throw new InvalidOperationException(ERROR_INVITATION_ALREADY_PARENT);
        }

        child.getParentIds().add(user.getId());
        childRepository.save(child);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(Instant.now());
        invitationRepository.save(invitation);

        log.info("Invitation accepted for child: {} by user: {}", child.getId(), user.getId());
    }

    @Override
    @Transactional
    public void revokeInvitationById(String invitationId) {
        ChildInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_INVITATION_NOT_FOUND));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
                throw new InvalidOperationException(ERROR_INVITATION_CANNOT_BE_REVOKED);
        }

        invitation.setStatus(InvitationStatus.REVOKED);
        invitation.setRevokedAt(Instant.now());
        invitationRepository.save(invitation);
    }
}