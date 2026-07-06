package com.infratrack.mobile.sync;

/**
 * Extension point for issuing and validating opaque sync cursors (BDR-005 / M5.2+).
 */
public interface SyncTokenService {

    /**
     * Issues the next opaque sync token for the client after a successful sync handshake.
     * The client stores the value only; the backend owns interpretation.
     */
    String resolveNextSyncToken(Long userId, String previousSyncToken, java.time.Instant watermark);
}
