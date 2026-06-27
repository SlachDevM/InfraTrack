package com.infratrack.inspection;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle status of an inspection")
public enum InspectionStatus {
    ASSIGNED,
    COMPLETED,
    CANCELLED
}
