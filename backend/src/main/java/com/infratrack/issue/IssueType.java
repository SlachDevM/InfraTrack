package com.infratrack.issue;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Business classification of an issue")
public enum IssueType {
    @Schema(description = "Issue identified from a completed inspection")
    NORMAL,
    @Schema(description = "Issue created from a completion review requiring rework")
    REWORK
}
