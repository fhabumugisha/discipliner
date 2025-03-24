package com.buseni.discipline.children.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.buseni.discipline.children.domain.ChildInvitation;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender emailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${twilio.account-sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token}")
    private String twilioAuthToken;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    public void sendInvitationEmail(ChildInvitation invitation) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(invitation.getInviteeEmail());
        message.setSubject("Invitation à co-gérer un enfant");
        message.setText(String.format("""
            Bonjour,
            
            Vous avez été invité(e) à co-gérer un enfant sur notre application.
            
            Pour accepter l'invitation, cliquez sur le lien suivant :
            %s/children/invitations/accept?token=%s
            
            Ce lien expirera dans 24 heures.
            
            Cordialement,
            L'équipe Discipline
            """,
            baseUrl,
            invitation.getToken()
        ));
        
        emailSender.send(message);
        log.info("Invitation email sent to: {}", invitation.getInviteeEmail());
    }

    public void sendInvitationSms(ChildInvitation invitation) {
        Twilio.init(twilioAccountSid, twilioAuthToken);
        
        String message = String.format("""
            Vous avez été invité(e) à co-gérer un enfant sur notre application.
            Pour accepter : %s/children/invitations/accept?token=%s
            """,
            baseUrl,
            invitation.getToken()
        );

        Message.creator(
            new PhoneNumber(invitation.getInviteePhone()),
            new PhoneNumber(twilioPhoneNumber),
            message
        ).create();

        log.info("Invitation SMS sent to: {}", invitation.getInviteePhone());
    }
} 