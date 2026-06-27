package com.infratrack.workorder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Operational priority for a work order")
public enum WorkOrderPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}
