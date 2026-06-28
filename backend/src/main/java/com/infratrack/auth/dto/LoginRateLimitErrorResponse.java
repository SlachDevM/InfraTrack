package com.infratrack.auth.dto;

public record LoginRateLimitErrorResponse(String message, long retryAfterSeconds) {
}
