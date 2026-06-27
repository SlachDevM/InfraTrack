package com.infratrack.inspection;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Operational priority for an assigned inspection")
public enum InspectionPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}
