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

        // Vérifier si l'enfant existe
        Child child = childRepository.findById(request.childId())
                .orElseThrow(() -> new IllegalArgumentException("Child not found"));

        // Vérifier le nombre de parents
        if (child.getParentIds().size() >= MAX_PARENTS) {
            throw new IllegalStateException("invitation.limit.reached");
        }

        // Générer un token unique pour l'invitation
        String token = UUID.randomUUID().toString();

        // Créer l'invitation
        ChildInvitation invitation = ChildInvitation.builder()
                .childId(child.getId())
                .childName(child.getName())
                .token(token)
                .inviteeEmail(request.email())
                .inviteePhone(request.phone())
                .expiresAt(Instant.now().plus(INVITATION_EXPIRY_HOURS, ChronoUnit.HOURS))
                .build();

        // Sauvegarder l'invitation
        invitationRepository.save(invitation);

        // Construire l'URL d'invitation
        String invitationUrl = buildInvitationUrl(token);

        // Envoyer l'invitation par email ou SMS
        if (request.email() != null && !request.email().isEmpty()) {
            // Vérifier si l'utilisateur existe déjà
            Optional<User> existingUser = userRepository.findByEmail(request.email());

            if (existingUser.isPresent()) {
                // L'utilisateur existe, envoyer un email d'invitation standard
                emailService.sendInvitation(request.email(), child.getName(), invitationUrl);
            } else {
                // L'utilisateur n'existe pas, envoyer un email avec lien d'inscription
                emailService.sendInvitationWithRegistration(request.email(), child.getName(), invitationUrl);
            }
        } else if (request.phone() != null && !request.phone().isEmpty()) {
            // Vérifier si l'utilisateur existe déjà avec ce numéro de téléphone
            Optional<User> existingUser = userRepository.findByPhone(request.phone());

            if (existingUser.isPresent()) {
                // L'utilisateur existe, envoyer un SMS d'invitation standard
                smsService.sendInvitation(request.phone(), child.getName(), invitationUrl);
            } else {
                // L'utilisateur n'existe pas, envoyer un SMS avec lien d'inscription
                smsService.sendInvitationWithRegistration(request.phone(), child.getName(), invitationUrl);
            }
        }

        log.info("Invitation created successfully for child: {} with token: {}", child.getId(), token);
    }

    @Override
    @Transactional
    public void acceptInvitation(String token) {
        ChildInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("invitation.invalid"));

        // Vérifier si l'invitation n'a pas expiré
        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("invitation.expired");
        }

        Child child = childRepository.findById(invitation.getChildId())
                .orElseThrow(() -> new IllegalArgumentException("Child not found"));

        // Vérifier le nombre de parents
        if (child.getParentIds().size() >= MAX_PARENTS) {
            throw new IllegalStateException("invitation.limit.reached");
        }

        // Trouver l'utilisateur par email ou téléphone
        User user = findUserByEmailOrPhone(invitation.getInviteeEmail(), invitation.getInviteePhone())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Vérifier si l'utilisateur n'est pas déjà parent
        if (child.getParentIds().contains(user.getId())) {
            throw new IllegalStateException("invitation.already.parent");
        }

        // Ajouter le parent
        child.getParentIds().add(user.getId());
        childRepository.save(child);

        // Marquer l'invitation comme acceptée
        invitation.setAcceptedAt(Instant.now());
        invitationRepository.save(invitation);

        log.info("Invitation accepted for child: {} by user: {}", child.getId(), user.getId());
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
                .orElseThrow(() -> new IllegalArgumentException("Parent not found"));
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
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Invitation cannot be accepted");
        }

        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Invitation expired");
        }

        Child child = childRepository.findById(invitation.getChildId())
                .orElseThrow(() -> new IllegalArgumentException("Child not found"));

        // Vérifier le nombre de parents
        if (child.getParentIds().size() >= MAX_PARENTS) {
            throw new IllegalStateException("invitation.limit.reached");
        }

        // Trouver l'utilisateur par email ou téléphone
        User user = findUserByEmailOrPhone(invitation.getInviteeEmail(), invitation.getInviteePhone())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Vérifier si l'utilisateur n'est pas déjà parent
        if (child.getParentIds().contains(user.getId())) {
            throw new IllegalStateException("invitation.already.parent");
        }

        // Ajouter le parent
        child.getParentIds().add(user.getId());
        childRepository.save(child);

        // Marquer l'invitation comme acceptée
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(Instant.now());
        invitationRepository.save(invitation);

        log.info("Invitation accepted for child: {} by user: {}", child.getId(), user.getId());
    }

    @Override
    @Transactional
    public void revokeInvitationById(String invitationId) {
        ChildInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Invitation cannot be revoked");
        }

        invitation.setStatus(InvitationStatus.REVOKED);
        invitation.setRevokedAt(Instant.now());
        invitationRepository.save(invitation);
    }
}