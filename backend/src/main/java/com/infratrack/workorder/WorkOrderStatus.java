package com.infratrack.workorder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle status of a work order")
public enum WorkOrderStatus {
    CREATED,
    ASSIGNED,
    COMPLETED,
    CANCELLED
}
