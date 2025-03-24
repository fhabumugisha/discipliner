package com.buseni.discipline.children.dto;

import java.time.Instant;

import com.buseni.discipline.children.domain.ChildInvitation.InvitationStatus;

public record ChildPendingInvitationDto(
    String id,
    String childName,
    InvitationStatus status,
    Instant expiresAt,
    Instant createdAt) {}