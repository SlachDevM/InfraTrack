package com.infratrack.delegatedauthority.dto;

import com.infratrack.delegatedauthority.DelegatedAuthority;

import java.time.LocalDateTime;

public class DelegatedAuthorityResponse {

    private Long id;
    private Long delegatingManagerUserId;
    private Long delegateManagerUserId;
    private Long sourceDepartmentId;
    private String sourceDepartmentName;
    private Long targetDepartmentId;
    private String targetDepartmentName;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String reason;
    private boolean revoked;
    private LocalDateTime revokedAt;
    private Long revokedByUserId;
    private Long createdAt;
    private Long updatedAt;

    public static DelegatedAuthorityResponse from(DelegatedAuthority authority) {
        DelegatedAuthorityResponse response = new DelegatedAuthorityResponse();
        response.id = authority.getId();
        response.delegatingManagerUserId = authority.getDelegatingManagerUserId();
        response.delegateManagerUserId = authority.getDelegateManagerUserId();
        response.sourceDepartmentId = authority.getSourceDepartment().getId();
        response.sourceDepartmentName = authority.getSourceDepartment().getName();
        response.targetDepartmentId = authority.getTargetDepartment().getId();
        response.targetDepartmentName = authority.getTargetDepartment().getName();
        response.validFrom = authority.getValidFrom();
        response.validUntil = authority.getValidUntil();
        response.reason = authority.getReason();
        response.revoked = authority.isRevoked();
        response.revokedAt = authority.getRevokedAt();
        response.revokedByUserId = authority.getRevokedByUserId();
        response.createdAt = authority.getCreatedAt();
        response.updatedAt = authority.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getDelegatingManagerUserId() {
        return delegatingManagerUserId;
    }

    public Long getDelegateManagerUserId() {
        return delegateManagerUserId;
    }

    public Long getSourceDepartmentId() {
        return sourceDepartmentId;
    }

    public String getSourceDepartmentName() {
        return sourceDepartmentName;
    }

    public Long getTargetDepartmentId() {
        return targetDepartmentId;
    }

    public String getTargetDepartmentName() {
        return targetDepartmentName;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public String getReason() {
        return reason;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public Long getRevokedByUserId() {
        return revokedByUserId;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
