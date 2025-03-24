package com.buseni.discipline.children.service;

public interface EmailService {
    void sendInvitation(String email, String childName, String invitationUrl);
    void sendInvitationWithRegistration(String email, String childName, String invitationUrl);
} 