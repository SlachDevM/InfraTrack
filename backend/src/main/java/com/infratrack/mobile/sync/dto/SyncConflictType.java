package com.infratrack.mobile.sync.dto;

public enum SyncConflictType {
    ENTITY_MODIFIED,
    ENTITY_DELETED,
    WORKFLOW_COMPLETED,
    VERSION_MISMATCH,
    PERMISSION_DENIED,
    UNKNOWN
}
