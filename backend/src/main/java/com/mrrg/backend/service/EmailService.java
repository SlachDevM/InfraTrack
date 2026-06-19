package com.mrrg.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.activation-link-base-url:mrrg://activate-account}")
    private String activationLinkBaseUrl;

    @Value("${spring.mail.from:noreply@mrrg.local}")
    private String fromEmail;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * Sends an account activation email to the user.
     * In development, logs the activation link instead of sending email.
     * In production, would use Spring Mail to send the actual email.
     *
     * SECURITY: Activation tokens are NEVER logged in production environments.
     * They are only logged in dev/development/local profiles for testing purposes.
     *
     * @param email the recipient email
     * @param token the activation token
     * @param userName the user's name for personalization
     */
    public void sendActivationEmail(String email, String token, String userName) {
        String activationLink = buildActivationLink(token);

        if (isDevelopment()) {
            logActivationLink(email, activationLink, userName);
        } else {
            sendEmailViaSMTP(email, activationLink, userName);
        }
    }

    private String buildActivationLink(String token) {
        if (activationLinkBaseUrl.startsWith("http")) {
            // Web-based link
            return activationLinkBaseUrl + "?token=" + token;
        } else {
            // Deep link format (e.g., mrrg://activate-account?token=...)
            return activationLinkBaseUrl + "?token=" + token;
        }
    }

    private boolean isDevelopment() {
        return "dev".equalsIgnoreCase(activeProfile) 
                || "development".equalsIgnoreCase(activeProfile) 
                || "local".equalsIgnoreCase(activeProfile);
    }

    /**
     * Logs the activation link. This should ONLY be called in development profiles.
     * SECURITY WARNING: This logs the activation token in plain text.
     * This must never be used in production.
     */
    private void logActivationLink(String email, String link, String userName) {
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("ACCOUNT ACTIVATION LINK (Development Mode Only)");
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("To: {}", email);
        log.info("User: {}", userName);
        log.info("Activation Link:");
        log.info("{}", link);
        log.info("═══════════════════════════════════════════════════════════════");
    }

    private void sendEmailViaSMTP(String email, String link, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Account Activation - MRRG");
            message.setText(buildActivationEmailBody(userName, link));
            
            mailSender.send(message);
            log.info("Activation email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send activation email to {}: {}", email, e.getMessage(), e);
        }
    }

    private String buildActivationEmailBody(String userName, String link) {
        return String.format(
            "Hello %s,\n\n" +
            "Welcome to MRRG! Your account has been created and is ready to activate.\n\n" +
            "Please click the link below to activate your account:\n" +
            "%s\n\n" +
            "This link will expire in 24 hours.\n\n" +
            "If you did not create this account, please contact an administrator.\n\n" +
            "Best regards,\n" +
            "MRRG Team",
            userName, link
        );
    }

    /**
     * Sends a notification email when a user's email address is changed by an admin.
     * This is a security notification to alert the user of the email change.
     *
     * @param oldEmail the previous email address
     * @param newEmail the new email address
     * @param userName the user's name
     */
    public void sendEmailChangeNotification(String oldEmail, String newEmail, String userName) {
        if (isDevelopment()) {
            logEmailChangeNotification(oldEmail, newEmail, userName);
        } else {
            sendEmailChangeViaSMTP(oldEmail, newEmail, userName);
        }
    }

    private void logEmailChangeNotification(String oldEmail, String newEmail, String userName) {
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("EMAIL CHANGE NOTIFICATION (Development Mode Only)");
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("User: {}", userName);
        log.info("Old Email: {}", oldEmail);
        log.info("New Email: {}", newEmail);
        log.info("═══════════════════════════════════════════════════════════════");
    }

    private void sendEmailChangeViaSMTP(String oldEmail, String newEmail, String userName) {
        try {
            // Send notification to old email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(oldEmail);
            message.setSubject("Email Address Changed - MRRG Account");
            message.setText(buildEmailChangeNotificationBody(userName, oldEmail, newEmail));
            
            mailSender.send(message);
            log.info("Email change notification sent to old email: {}", oldEmail);
        } catch (Exception e) {
            log.error("Failed to send email change notification to {}: {}", oldEmail, e.getMessage(), e);
        }
    }

    private String buildEmailChangeNotificationBody(String userName, String oldEmail, String newEmail) {
        return String.format(
            "Hello %s,\n\n" +
            "This is a security notification to inform you that your email address has been changed.\n\n" +
            "Old Email: %s\n" +
            "New Email: %s\n\n" +
            "If you did not authorize this change, please contact an administrator immediately.\n\n" +
            "Best regards,\n" +
            "MRRG Team",
            userName, oldEmail, newEmail
        );
    }
}
