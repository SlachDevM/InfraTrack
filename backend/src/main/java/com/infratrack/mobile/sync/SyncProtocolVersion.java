package com.infratrack.mobile.sync;

/**
 * Mobile sync protocol version exposed in {@link com.infratrack.mobile.sync.dto.SyncResponse}.
 * Increment only when additive JSON fields are insufficient; clients must tolerate unknown fields.
 */
public final class SyncProtocolVersion {

    public static final int CURRENT = 1;

    private SyncProtocolVersion() {
    }
}
