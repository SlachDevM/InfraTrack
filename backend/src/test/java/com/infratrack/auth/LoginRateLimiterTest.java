package com.infratrack.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginRateLimiterTest {

    private LoginRateLimiter loginRateLimiter;

    @BeforeEach
    void setUp() {
        loginRateLimiter = new LoginRateLimiter(3);
    }

    @Test
    void checkAllowed_shouldPermitAttemptsWithinLimit() {
        assertThatCode(() -> {
            loginRateLimiter.checkAllowed("192.168.1.10", "user@example.com");
            loginRateLimiter.checkAllowed("192.168.1.10", "user@example.com");
            loginRateLimiter.checkAllowed("192.168.1.10", "user@example.com");
        }).doesNotThrowAnyException();
    }

    @Test
    void checkAllowed_shouldReturn429WhenIpLimitExceeded() {
        for (int attempt = 0; attempt < 3; attempt++) {
            loginRateLimiter.checkAllowed("10.0.0.1", "first@example.com");
        }

        assertThatThrownBy(() -> loginRateLimiter.checkAllowed("10.0.0.1", "second@example.com"))
                .isInstanceOf(LoginRateLimitExceededException.class)
                .satisfies(exception -> {
                    LoginRateLimitExceededException rateLimitException = (LoginRateLimitExceededException) exception;
                    assertThat(rateLimitException.getMessage()).isEqualTo(LoginRateLimiter.RATE_LIMIT_MESSAGE);
                    assertThat(rateLimitException.getRetryAfterSeconds()).isPositive();
                });
    }

    @Test
    void checkAllowed_shouldReturn429WhenEmailLimitExceeded() {
        for (int attempt = 0; attempt < 3; attempt++) {
            loginRateLimiter.checkAllowed("10.0.0." + attempt, "shared@example.com");
        }

        assertThatThrownBy(() -> loginRateLimiter.checkAllowed("10.0.0.99", "shared@example.com"))
                .isInstanceOf(LoginRateLimitExceededException.class)
                .satisfies(exception -> {
                    LoginRateLimitExceededException rateLimitException = (LoginRateLimitExceededException) exception;
                    assertThat(rateLimitException.getMessage()).isEqualTo(LoginRateLimiter.RATE_LIMIT_MESSAGE);
                    assertThat(rateLimitException.getRetryAfterSeconds()).isPositive();
                });
    }

    @Test
    void checkAllowed_shouldUseGenericMessageWithoutLeakingEmailExistence() {
        assertThat(LoginRateLimiter.RATE_LIMIT_MESSAGE)
                .doesNotContain("email")
                .doesNotContain("user");
    }

    @Test
    void checkAllowed_shouldNormalizeEmailForRateLimitKey() {
        for (int attempt = 0; attempt < 3; attempt++) {
            loginRateLimiter.checkAllowed("10.0.0.5", "User@Example.com");
        }

        assertThatThrownBy(() -> loginRateLimiter.checkAllowed("10.0.0.6", "user@example.com"))
                .isInstanceOf(LoginRateLimitExceededException.class);
    }
}
