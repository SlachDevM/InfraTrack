package com.infratrack.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(
                jwtTokenProvider,
                "jwtSecret",
                "IntegrationTestJwtSecretKeyMustBeLongEnoughForHS512Algorithm123456789"
        );
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 86400000);
        jwtTokenProvider.initializeSigningKey();
    }

    @Test
    void generateToken_shouldProduceValidToken() {
        String token = jwtTokenProvider.generateToken(42L, "user@example.com");

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_shouldRejectInvalidToken() {
        assertThat(jwtTokenProvider.validateToken("invalid-token")).isFalse();
    }

    @Test
    void getEmailFromToken_shouldReturnSubject() {
        String token = jwtTokenProvider.generateToken(7L, "manager@example.com");

        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo("manager@example.com");
    }

    @Test
    void getUserIdFromToken_shouldReturnClaim() {
        String token = jwtTokenProvider.generateToken(99L, "manager@example.com");

        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(99L);
    }

    @Test
    void initializeSigningKey_shouldReuseCachedKeyForSubsequentOperations() {
        Object cachedKey = ReflectionTestUtils.getField(jwtTokenProvider, "signingKey");

        String token = jwtTokenProvider.generateToken(1L, "user@example.com");
        jwtTokenProvider.validateToken(token);

        assertThat(ReflectionTestUtils.getField(jwtTokenProvider, "signingKey")).isSameAs(cachedKey);
    }
}
