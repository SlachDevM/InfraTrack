package com.mrrg.backend.model;

/**
 * Represents the activation/availability status of a user.
 *
 * PENDING_ACTIVATION: User has been invited but has not yet activated their account.
 * ACTIVE: User is fully activated and can log in.
 * DISABLED: User has been deactivated by an administrator and cannot log in.
 */
public enum UserStatus {
    PENDING_ACTIVATION,
    ACTIVE,
    DISABLED
}
