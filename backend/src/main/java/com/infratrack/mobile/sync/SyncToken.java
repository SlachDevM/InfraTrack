package com.infratrack.mobile.sync;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Opaque sync cursor issued by the backend. Android stores {@link #toOpaqueValue()} only;
 * the backend owns encoding and future interpretation.
 */
public final class SyncToken {

    private final int version;
    private final Instant issuedAt;
    private final String token;

    public SyncToken(int version, Instant issuedAt, String token) {
        this.version = version;
        this.issuedAt = Objects.requireNonNull(issuedAt, "issuedAt");
        this.token = Objects.requireNonNull(token, "token");
    }

    public static SyncToken issue(Instant issuedAt) {
        return new SyncToken(SyncProtocolVersion.CURRENT, issuedAt, UUID.randomUUID().toString());
    }

    public int getVersion() {
        return version;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public String getToken() {
        return token;
    }

    /**
     * Encodes this token as an opaque string for API transport. Not intended for client parsing.
     */
    public String toOpaqueValue() {
        return SyncTokenCodec.encode(this);
    }

    public static SyncToken fromOpaqueValue(String opaqueValue) {
        return SyncTokenCodec.decode(opaqueValue);
    }
}
