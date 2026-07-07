package com.infratrack.workorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public class SaveWorkOrderProgressRequest {

    @Size(max = 4000)
    @Schema(description = "Draft maintenance completion notes (does not complete the work order)")
    private String completionNotes;

    public String getCompletionNotes() {
        return completionNotes;
    }

    public void setCompletionNotes(String completionNotes) {
        this.completionNotes = completionNotes;
    }
}
