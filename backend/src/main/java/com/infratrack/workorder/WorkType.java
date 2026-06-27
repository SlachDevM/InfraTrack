package com.infratrack.workorder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type of maintenance work for a work order assignment")
public enum WorkType {
    INTERNAL_MAINTENANCE,
    CONTRACTOR_WORK
}
