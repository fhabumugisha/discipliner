package com.buseni.discipline.children.service;

public interface SmsService {
    /**
     * Send an invitation SMS to the invitee
     * @param phone the phone number of the invitee
     * @param childName the name of the child
     * @param invitationUrl the URL of the invitation
     */ 
    void sendInvitation(String phone, String childName, String invitationUrl);
    /**
     * Send an invitation SMS to the invitee with a registration link
     * @param phone the phone number of the invitee
     * @param childName the name of the child
     * @param invitationUrl the URL of the invitation
     */
    void sendInvitationWithRegistration(String phone, String childName, String invitationUrl);
} 