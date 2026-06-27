package com.infratrack.mail;

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

    @Value("${app.activation-link-base-url:app://activate-account}")
    private String activationLinkBaseUrl;

    @Value("${spring.mail.from:noreply@infratrack.local}")
    private String fromEmail;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * Sends an account activation email to the user.
     * When SMTP is configured (including Docker dev with Mailpit), sends the email.
     * When no mail sender is available in a development profile, logs the activation link instead.
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

        if (mailSender != null) {
            sendEmailViaSMTP(email, activationLink, userName);
        } else if (isDevelopment()) {
            logActivationLink(email, activationLink, userName);
        } else {
            log.error("Cannot send activation email to {}: mail sender is not configured", email);
        }
    }

    private String buildActivationLink(String token) {
        if (activationLinkBaseUrl.startsWith("http")) {
            // Web-based link
            return activationLinkBaseUrl + "?token=" + token;
        } else {
            // Deep link format (e.g., app://activate-account?token=...)
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
            message.setSubject("Account Activation - InfraTrack");
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
            "Welcome to InfraTrack! Your account has been created and is ready to activate.\n\n" +
            "Please click the link below to activate your account:\n" +
            "%s\n\n" +
            "This link will expire in 24 hours.\n\n" +
            "If you did not create this account, please contact an administrator.\n\n" +
            "Best regards,\n" +
            "InfraTrack Team",
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
        if (mailSender != null) {
            sendEmailChangeViaSMTP(oldEmail, newEmail, userName);
        } else if (isDevelopment()) {
            logEmailChangeNotification(oldEmail, newEmail, userName);
        } else {
            log.error("Cannot send email change notification to {}: mail sender is not configured", oldEmail);
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
            message.setSubject("Email Address Changed - InfraTrack Account");
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
            "InfraTrack Team",
            userName, oldEmail, newEmail
        );
    }
}
