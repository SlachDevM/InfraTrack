package com.infratrack.inspectiontemplate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle status of an inspection template")
public enum InspectionTemplateStatus {
    @Schema(description = "Template is being prepared and is not yet published")
    DRAFT,
    @Schema(description = "Template is published for operational use")
    PUBLISHED,
    @Schema(description = "Template is retired and retained for traceability")
    ARCHIVED
}
