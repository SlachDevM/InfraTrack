package com.infratrack.mobile.sync.dto;

/**
 * Outcome of an explicit conflict resolution request (M5.5-BE2).
 */
public enum SyncConflictResolutionStatus {
    RESOLVED,
    RETRY_REQUIRED,
    MANUAL_REVIEW_REQUIRED,
    REJECTED
}
