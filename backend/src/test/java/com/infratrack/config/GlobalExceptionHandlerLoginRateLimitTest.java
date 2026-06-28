package com.infratrack.config;

import com.infratrack.auth.LoginRateLimitExceededException;
import com.infratrack.auth.LoginRateLimiter;
import com.infratrack.auth.dto.LoginRateLimitErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerLoginRateLimitTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleLoginRateLimitExceededException_shouldReturnRetryAfterHeaderAndBody() {
        ResponseEntity<LoginRateLimitErrorResponse> response = handler.handleLoginRateLimitExceededException(
                new LoginRateLimitExceededException(LoginRateLimiter.RATE_LIMIT_MESSAGE, 60)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)).isEqualTo("60");
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo(LoginRateLimiter.RATE_LIMIT_MESSAGE);
        assertThat(response.getBody().retryAfterSeconds()).isEqualTo(60);
    }

    @Test
    void handleLoginRateLimitExceededException_shouldKeepHeaderAndBodyInSync() {
        ResponseEntity<LoginRateLimitErrorResponse> response = handler.handleLoginRateLimitExceededException(
                new LoginRateLimitExceededException(LoginRateLimiter.RATE_LIMIT_MESSAGE, 42)
        );

        assertThat(response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER))
                .isEqualTo(String.valueOf(response.getBody().retryAfterSeconds()));
    }
}
