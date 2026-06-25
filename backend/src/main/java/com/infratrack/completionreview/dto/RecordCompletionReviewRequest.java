package com.infratrack.completionreview.dto;

import com.infratrack.completionreview.CompletionReviewDecision;

import java.time.LocalDateTime;

public class RecordCompletionReviewRequest {

    private CompletionReviewDecision decision;
    private String reviewNotes;
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
