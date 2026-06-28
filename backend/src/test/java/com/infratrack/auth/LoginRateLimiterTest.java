package com.infratrack.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

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
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseStatusException = (ResponseStatusException) exception;
                    assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
                    assertThat(responseStatusException.getReason()).isEqualTo(LoginRateLimiter.RATE_LIMIT_MESSAGE);
                });
    }

    @Test
    void checkAllowed_shouldReturn429WhenEmailLimitExceeded() {
        for (int attempt = 0; attempt < 3; attempt++) {
            loginRateLimiter.checkAllowed("10.0.0." + attempt, "shared@example.com");
        }

        assertThatThrownBy(() -> loginRateLimiter.checkAllowed("10.0.0.99", "shared@example.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseStatusException = (ResponseStatusException) exception;
                    assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
                    assertThat(responseStatusException.getReason()).isEqualTo(LoginRateLimiter.RATE_LIMIT_MESSAGE);
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
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
