package com.infratrack.delegatedauthority.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class CreateDelegatedAuthorityRequest {

    @NotNull
    @Positive
    private Long delegateManagerUserId;

    @NotNull
    @Positive
    private Long sourceDepartmentId;

    @NotNull
    @Positive
    private Long targetDepartmentId;

    @NotNull
    private LocalDateTime validFrom;

    @NotNull
    private LocalDateTime validUntil;

    @NotBlank
    @Size(max = 4000)
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
