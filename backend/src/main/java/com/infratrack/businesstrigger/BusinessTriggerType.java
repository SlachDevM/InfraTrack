package com.infratrack.businesstrigger;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Reason category for an operational business trigger (UC-006)")
public enum BusinessTriggerType {
    SCHEDULED_INSPECTION,
    CUSTOMER_REQUEST,
    EMERGENCY_EVENT,
    MANAGER_REQUEST,
    FIELD_OBSERVATION
}
