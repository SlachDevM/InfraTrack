package com.infratrack.issue;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Business severity of an identified issue")
public enum IssueSeverity {
    @Schema(description = "Minor impact; routine follow-up")
    LOW,
    @Schema(description = "Moderate impact")
    MEDIUM,
    @Schema(description = "Significant impact requiring priority attention")
    HIGH,
    @Schema(description = "Immediate safety or service risk")
    CRITICAL
}
