package com.infratrack.mobile.sync.dto;

/**
 * Informational guidance for clients presenting sync conflicts (M5.5-BE1.1).
 * Does not trigger automatic resolution on the server.
 */
public enum SyncResolutionHint {
    SERVER_WINS,
    CLIENT_RETRY,
    MANUAL_REVIEW,
    UNKNOWN
}
