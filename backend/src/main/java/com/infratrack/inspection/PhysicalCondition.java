package com.infratrack.inspection;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Observed physical condition recorded during inspection completion")
public enum PhysicalCondition {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    CRITICAL
}
