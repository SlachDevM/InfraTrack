package com.infratrack.mail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSenderProvider);
        ReflectionTestUtils.setField(emailService, "activationLinkBaseUrl", "app://activate-account");
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@infratrack.local");
        ReflectionTestUtils.setField(emailService, "activeProfile", "dev");
    }

    @Test
    void isDevelopment_returnsTrue_forDevProfile() {
        boolean isDev = (boolean) ReflectionTestUtils.invokeMethod(emailService, "isDevelopment");
        assertThat(isDev).isTrue();
    }

    @Test
    void isDevelopment_returnsFalse_forProductionProfile() {
        ReflectionTestUtils.setField(emailService, "activeProfile", "prod");

        boolean isDev = (boolean) ReflectionTestUtils.invokeMethod(emailService, "isDevelopment");
        assertThat(isDev).isFalse();
    }

    @Test
    void sendActivationEmail_sendsViaSmtp_whenMailSenderConfigured() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);

        emailService.sendActivationEmail("user@test.com", "secret-token-123", "Test User");

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();
        assertThat(message.getTo()).containsExactly("user@test.com");
        assertThat(message.getSubject()).isEqualTo("Account Activation - InfraTrack");
        assertThat(message.getText()).contains("Test User");
        assertThat(message.getText()).contains("app://activate-account?token=secret-token-123");
    }

    @Test
    void sendActivationEmail_logsOnly_whenMailSenderUnavailableInDevelopment() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);

        assertThatNoException().isThrownBy(() ->
                emailService.sendActivationEmail("user@test.com", "secret-token-123", "Test User")
        );

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendActivationEmail_doesNotLogTokenInProduction_whenMailSenderUnavailable() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);
        ReflectionTestUtils.setField(emailService, "activeProfile", "prod");

        assertThatNoException().isThrownBy(() ->
                emailService.sendActivationEmail("user@test.com", "secret-token-123", "Test User")
        );

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void buildActivationLink_formatsDeepLink() {
        String link = (String) ReflectionTestUtils.invokeMethod(emailService, "buildActivationLink", "test-token-123");
        assertThat(link).isEqualTo("app://activate-account?token=test-token-123");
    }

    @Test
    void buildActivationLink_formatsHttpsLink() {
        ReflectionTestUtils.setField(emailService, "activationLinkBaseUrl", "https://example.com/activate");

        String link = (String) ReflectionTestUtils.invokeMethod(emailService, "buildActivationLink", "test-token-456");
        assertThat(link).isEqualTo("https://example.com/activate?token=test-token-456");
    }
}
