package com.infratrack.completionreview;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Manager completion review decision (UC-010)")
public enum CompletionReviewDecision {
    @Schema(description = "Maintenance work accepted")
    APPROVED,
    @Schema(description = "Further work required before acceptance")
    REWORK_REQUIRED
}
