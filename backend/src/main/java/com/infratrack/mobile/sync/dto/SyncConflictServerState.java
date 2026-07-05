package com.infratrack.mobile.sync.dto;

import com.infratrack.inspection.InspectionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Compact server entity snapshot for sync conflict presentation (M5.5-BE1.1).
 */
public class SyncConflictServerState {

    private Long entityId;

    @Schema(description = "Entity type", example = "INSPECTION")
    private String entityType;

    private InspectionStatus status;

    @Schema(description = "Server-side last update time (epoch millis)")
    private Long updatedAt;

    private LocalDateTime completedAt;

    @Schema(description = "Display name of the user who completed the entity, when known")
    private String completedBy;

    @Schema(description = "Assigned user id when applicable")
    private Long assignedTo;

    private String assignedToName;

    @Schema(description = "Entity version when the domain model exposes one")
    private Long version;

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public InspectionStatus getStatus() {
        return status;
    }

    public void setStatus(InspectionStatus status) {
        this.status = status;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(String completedBy) {
        this.completedBy = completedBy;
    }

    public Long getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Long assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
