package com.infratrack.mobile.sync;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

/**
 * Issues opaque sync tokens for successful sync handshakes (M5.2-BE2).
 * Does not encode business data; encoding may change without Android changes.
 */
@Service
class DefaultSyncTokenService implements SyncTokenService {

    private final Clock clock;

    DefaultSyncTokenService(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String resolveNextSyncToken(Long userId, String previousSyncToken, Instant watermark) {
        Instant issuedAt = watermark != null ? watermark : clock.instant();
        return SyncToken.issue(issuedAt).toOpaqueValue();
    }
}
