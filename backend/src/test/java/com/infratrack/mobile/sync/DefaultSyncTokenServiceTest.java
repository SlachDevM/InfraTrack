package com.infratrack.mobile.sync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultSyncTokenServiceTest {

    private static final Long USER_ID = 20L;
    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-05T08:30:00Z");

    private DefaultSyncTokenService syncTokenService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        syncTokenService = new DefaultSyncTokenService(clock);
    }

    @Test
    void resolveNextSyncToken_returnsOpaqueValue() {
        String token = syncTokenService.resolveNextSyncToken(USER_ID, null);

        assertThat(token).isNotBlank();
        SyncToken decoded = SyncToken.fromOpaqueValue(token);
        assertThat(decoded.getVersion()).isEqualTo(SyncProtocolVersion.CURRENT);
        assertThat(decoded.getIssuedAt()).isEqualTo(FIXED_INSTANT);
    }

    @Test
    void resolveNextSyncToken_ignoresPreviousTokenForNow() {
        String first = syncTokenService.resolveNextSyncToken(USER_ID, null);
        String second = syncTokenService.resolveNextSyncToken(USER_ID, first);

        assertThat(second).isNotEqualTo(first);
    }
}
