package com.buseni.discipline.children.dto;

import java.time.Instant;

import com.buseni.discipline.children.domain.ChildInvitation.InvitationStatus;

public record ChildInvitationDto(
    String id,
    String childId,
    String childName,
    String email,
    String phone,
    String token,
    InvitationStatus status,
    Instant expiresAt,
    Instant createdAt
) {} 