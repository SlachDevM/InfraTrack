package com.infratrack.mobile.dto;

import com.infratrack.maintenanceactivity.MaintenanceActivity;

import java.time.LocalDateTime;

public class MobileMaintenanceActivitySummaryResponse {

    private Long maintenanceActivityId;
    private String status;
    private String notes;
    private LocalDateTime completedAt;

    public static MobileMaintenanceActivitySummaryResponse from(MaintenanceActivity activity) {
        MobileMaintenanceActivitySummaryResponse response = new MobileMaintenanceActivitySummaryResponse();
        response.maintenanceActivityId = activity.getId();
        response.status = "COMPLETED";
        response.notes = activity.getCompletionNotes();
        response.completedAt = activity.getCompletedAt();
        return response;
    }

    public Long getMaintenanceActivityId() {
        return maintenanceActivityId;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
