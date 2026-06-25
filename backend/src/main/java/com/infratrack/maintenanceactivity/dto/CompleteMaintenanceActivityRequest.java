package com.infratrack.maintenanceactivity.dto;

import java.time.LocalDateTime;

public class CompleteMaintenanceActivityRequest {

    private String completionNotes;
    private LocalDateTime completedAt;

    public String getCompletionNotes() {
        return completionNotes;
    }

    public void setCompletionNotes(String completionNotes) {
        this.completionNotes = completionNotes;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
