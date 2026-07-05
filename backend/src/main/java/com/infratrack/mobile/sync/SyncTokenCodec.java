package com.infratrack.mobile.sync;

import com.infratrack.exception.BusinessValidationException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * Encodes and decodes {@link SyncToken} as an opaque API string.
 * Format (before Base64): {@code version|issuedAtEpochMillis|randomUuid}
 */
final class SyncTokenCodec {

    private static final String SEPARATOR = "|";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private SyncTokenCodec() {
    }

    static String encode(SyncToken syncToken) {
        String payload = syncToken.getVersion()
                + SEPARATOR
                + syncToken.getIssuedAt().toEpochMilli()
                + SEPARATOR
                + syncToken.getToken();
        return ENCODER.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    static SyncToken decode(String opaqueValue) {
        if (opaqueValue == null || opaqueValue.isBlank()) {
            throw new BusinessValidationException("Sync token is required.");
        }
        try {
            String payload = new String(DECODER.decode(opaqueValue), StandardCharsets.UTF_8);
            String[] parts = payload.split("\\|", 3);
            if (parts.length != 3) {
                throw new BusinessValidationException("Invalid sync token.");
            }
            int version = Integer.parseInt(parts[0]);
            Instant issuedAt = Instant.ofEpochMilli(Long.parseLong(parts[1]));
            return new SyncToken(version, issuedAt, parts[2]);
        } catch (IllegalArgumentException ex) {
            throw new BusinessValidationException("Invalid sync token.");
        }
    }
}
