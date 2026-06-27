package com.infratrack.completionreview.dto;

import com.infratrack.completionreview.CompletionReviewDecision;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class RecordCompletionReviewRequest {

    @NotNull
    private CompletionReviewDecision decision;

    @NotBlank
    @Size(max = 4000)
    private String reviewNotes;

    @NotNull
    private LocalDateTime reviewedAt;

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
}
