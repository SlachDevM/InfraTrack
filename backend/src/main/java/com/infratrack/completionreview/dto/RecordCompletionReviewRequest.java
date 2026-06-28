package com.infratrack.completionreview.dto;

import com.infratrack.completionreview.CompletionReviewDecision;
import com.infratrack.issue.IssueSeverity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class RecordCompletionReviewRequest {

    @NotNull
    @Schema(description = "Manager review decision for completed maintenance")
    private CompletionReviewDecision decision;

    @NotBlank
    @Size(max = 4000)
    private String reviewNotes;

    @NotNull
    private LocalDateTime reviewedAt;

    @Schema(description = "Severity for the rework issue when decision is REWORK_REQUIRED")
    private IssueSeverity reworkSeverity;

    @Size(max = 4000)
    private String rootCause;

    @Size(max = 4000)
    private String correctiveAction;

    @Size(max = 4000)
    private String preventiveAction;

    public CompletionReviewDecision getDecision() {
        return decision;
    }

    public void setDecision(CompletionReviewDecision decision) {
        this.decision = decision;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public IssueSeverity getReworkSeverity() {
        return reworkSeverity;
    }

    public void setReworkSeverity(IssueSeverity reworkSeverity) {
        this.reworkSeverity = reworkSeverity;
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public String getCorrectiveAction() {
        return correctiveAction;
    }

    public void setCorrectiveAction(String correctiveAction) {
        this.correctiveAction = correctiveAction;
    }

    public String getPreventiveAction() {
        return preventiveAction;
    }

    public void setPreventiveAction(String preventiveAction) {
        this.preventiveAction = preventiveAction;
    }
}
