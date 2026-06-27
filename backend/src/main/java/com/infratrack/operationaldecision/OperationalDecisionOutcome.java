package com.infratrack.operationaldecision;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Manager decision outcome for an issue (UC-007)")
public enum OperationalDecisionOutcome {
    @Schema(description = "Continue monitoring without immediate work")
    CONTINUE_MONITORING,
    @Schema(description = "Proceed with internal maintenance")
    INTERNAL_MAINTENANCE,
    @Schema(description = "Engage external contractor work")
    CONTRACTOR_WORK,
    @Schema(description = "Recommend asset renewal or replacement")
    RENEWAL_RECOMMENDATION,
    @Schema(description = "Recommend asset decommission")
    DECOMMISSION_RECOMMENDATION
}
