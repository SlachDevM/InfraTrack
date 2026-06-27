package com.infratrack.operationaldocument;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Operational record that owns an uploaded document")
public enum OperationalDocumentOwnerType {
    ASSET,
    INSPECTION,
    ISSUE,
    OPERATIONAL_DECISION,
    WORK_ORDER,
    MAINTENANCE_ACTIVITY,
    COMPLETION_REVIEW
}
