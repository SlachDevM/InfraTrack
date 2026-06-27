package com.infratrack.user;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents the activation/availability status of a user.
 */
@Schema(description = "User account lifecycle status")
public enum UserStatus {
    PENDING_ACTIVATION,
    ACTIVE,
    DISABLED
}
