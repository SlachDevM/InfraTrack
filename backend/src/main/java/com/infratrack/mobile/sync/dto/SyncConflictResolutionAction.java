package com.infratrack.mobile.sync.dto;

/**
 * Explicit conflict resolution decision submitted by the client (M5.5-BE2).
 * Does not trigger automatic server-side merge.
 */
public enum SyncConflictResolutionAction {
    SERVER_WINS,
    CLIENT_RETRY,
    MANUAL_REVIEW,
    DISCARD_CLIENT
}
