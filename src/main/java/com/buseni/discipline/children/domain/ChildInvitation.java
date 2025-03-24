package com.buseni.discipline.children.domain;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "child_invitations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildInvitation {
    
    @Id
    private String id;

    @Field("child_id")
    private String childId;

    @Field("child_name")
    private String childName;

    @Field("inviter_id")
    private String inviterId;

    @Field("invitee_email")
    private String inviteeEmail;

    @Field("invitee_phone")
    private String inviteePhone;

    @Field("token")
    @Builder.Default
    private String token = UUID.randomUUID().toString();

    @Field("status")
    @Builder.Default
    private InvitationStatus status = InvitationStatus.PENDING;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @Field("expires_at")
    private Instant expiresAt;

    @Field("accepted_at")
    private Instant acceptedAt;

    @Field("revoked_at")
    private Instant revokedAt;

    public enum InvitationStatus {
        PENDING,
        ACCEPTED,
        EXPIRED,
        REVOKED
    }
} 