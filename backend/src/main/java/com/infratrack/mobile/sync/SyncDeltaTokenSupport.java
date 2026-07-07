package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.SyncWarningCode;
import com.infratrack.mobile.sync.dto.SyncWarningResponse;

import java.util.List;
import java.util.Optional;

/**
 * Shared sync-token interpretation for delta download sections (inspections, work orders).
 */
final class SyncDeltaTokenSupport {

    static final String INVALID_TOKEN_MESSAGE =
            "Sync token is invalid; returning full sync delta.";

    static final String UNSUPPORTED_PROTOCOL_MESSAGE =
            "Sync token protocol version is unsupported; returning full sync delta.";

    private SyncDeltaTokenSupport() {
    }

    static Long resolveUpdatedSinceMillis(String previousSyncToken, List<SyncWarningResponse> warnings) {
        Optional<SyncToken> previousToken = SyncToken.tryFromOpaqueValue(previousSyncToken);
        if (previousSyncToken == null || previousSyncToken.isBlank()) {
            return null;
        }
        if (previousToken.isEmpty()) {
            warnings.add(new SyncWarningResponse(SyncWarningCode.FULL_SYNC_REQUIRED, INVALID_TOKEN_MESSAGE));
            return null;
        }
        if (previousToken.get().getVersion() != SyncProtocolVersion.CURRENT) {
            warnings.add(new SyncWarningResponse(
                    SyncWarningCode.FULL_SYNC_REQUIRED,
                    UNSUPPORTED_PROTOCOL_MESSAGE));
            return null;
        }
        return previousToken.get().getIssuedAt().toEpochMilli();
    }
}
