package com.infratrack.delegatedauthority.dto;

import java.time.LocalDateTime;

public class CreateDelegatedAuthorityRequest {

    private Long delegateManagerUserId;
    private Long sourceDepartmentId;
    private Long targetDepartmentId;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String reason;

    public Long getDelegateManagerUserId() {
        return delegateManagerUserId;
    }

    public void setDelegateManagerUserId(Long delegateManagerUserId) {
        this.delegateManagerUserId = delegateManagerUserId;
    }

    public Long getSourceDepartmentId() {
        return sourceDepartmentId;
    }

    public void setSourceDepartmentId(Long sourceDepartmentId) {
        this.sourceDepartmentId = sourceDepartmentId;
    }

    public Long getTargetDepartmentId() {
        return targetDepartmentId;
    }

    public void setTargetDepartmentId(Long targetDepartmentId) {
        this.targetDepartmentId = targetDepartmentId;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
