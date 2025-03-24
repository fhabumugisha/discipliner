package com.buseni.discipline.children.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.buseni.discipline.children.service.SmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromNumber;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    @Override
    public void sendInvitation(String phone, String childName, String invitationUrl) {
        log.debug("Sending invitation SMS to: {} for child: {}", phone, childName);
        
        String messageText = String.format(
            "Vous avez été invité(e) à rejoindre la famille de %s dans l'application Discipliner. " +
            "Pour accepter l'invitation, cliquez ici : %s",
            childName, invitationUrl
        );

        Message message = Message.creator(
            new PhoneNumber(phone),
            new PhoneNumber(fromNumber),
            messageText
        ).create();

        log.info("Invitation SMS sent successfully to: {} with sid: {}", phone, message.getSid());
    }

    @Override
    public void sendInvitationWithRegistration(String phone, String childName, String invitationUrl) {
        log.debug("Sending invitation with registration SMS to: {} for child: {}", phone, childName);
        
        String messageText = String.format(
            "Vous avez été invité(e) à rejoindre la famille de %s dans l'application Discipliner. " +
            "1. Créez votre compte : %s/auth/register?phone=%s " +
            "2. Acceptez l'invitation : %s",
            childName, appBaseUrl, phone, invitationUrl
        );

        Message message = Message.creator(
            new PhoneNumber(phone),
            new PhoneNumber(fromNumber),
            messageText
        ).create();

        log.info("Invitation with registration SMS sent successfully to: {} with sid: {}", phone, message.getSid());
    }
} 