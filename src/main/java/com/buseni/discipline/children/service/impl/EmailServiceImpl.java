package com.buseni.discipline.children.service.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.buseni.discipline.children.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendInvitation(String email, String childName, String invitationUrl) {
        log.debug("Sending invitation email to: {} for child: {}", email, childName);
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Invitation à rejoindre la famille de " + childName);
        message.setText("""
            Bonjour,
            
            Vous avez été invité(e) à rejoindre la famille de %s dans l'application Discipliner.
            
            Pour accepter l'invitation, cliquez sur le lien suivant :
            %s
            
            Cette invitation expirera dans 24 heures.
            
            Cordialement,
            L'équipe Discipliner
            """.formatted(childName, invitationUrl));

        mailSender.send(message);
        log.info("Invitation email sent successfully to: {}", email);
    }

    @Override
    public void sendInvitationWithRegistration(String email, String childName, String invitationUrl) {
        log.debug("Sending invitation with registration email to: {} for child: {}", email, childName);
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Invitation à rejoindre la famille de " + childName);
        message.setText("""
            Bonjour,
            
            Vous avez été invité(e) à rejoindre la famille de %s dans l'application Discipliner.
            
            Pour commencer :
            1. Créez votre compte en cliquant sur ce lien : /auth/register?email=%s
            2. Une fois inscrit(e), acceptez l'invitation en cliquant ici : %s
            
            Cette invitation expirera dans 24 heures.
            
            Cordialement,
            L'équipe Discipliner
            """.formatted(childName, email, invitationUrl));

        mailSender.send(message);
        log.info("Invitation with registration email sent successfully to: {}", email);
    }
} 