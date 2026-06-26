package com.infratrack.delegatedauthority;

import com.infratrack.department.Department;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "delegated_authorities")
public class DelegatedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delegating_manager_user_id", nullable = false)
    private Long delegatingManagerUserId;

    @Column(name = "delegate_manager_user_id", nullable = false)
    private Long delegateManagerUserId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "source_department_id", nullable = false)
    private Department sourceDepartment;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "target_department_id", nullable = false)
    private Department targetDepartment;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoked_by_user_id")
    private Long revokedByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    protected DelegatedAuthority() {
    }

    public DelegatedAuthority(
            Long delegatingManagerUserId,
            Long delegateManagerUserId,
            Department sourceDepartment,
            Department targetDepartment,
            LocalDateTime validFrom,
            LocalDateTime validUntil,
            String reason) {
        this.delegatingManagerUserId = delegatingManagerUserId;
        this.delegateManagerUserId = delegateManagerUserId;
        this.sourceDepartment = sourceDepartment;
        this.targetDepartment = targetDepartment;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.reason = reason;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDelegatingManagerUserId() {
        return delegatingManagerUserId;
    }

    public Long getDelegateManagerUserId() {
        return delegateManagerUserId;
    }

    public Department getSourceDepartment() {
        return sourceDepartment;
    }

    public Department getTargetDepartment() {
        return targetDepartment;
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

    public void revoke(Long revokedByUserId, LocalDateTime revokedAt) {
        this.revoked = true;
        this.revokedByUserId = revokedByUserId;
        this.revokedAt = revokedAt;
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isActiveAt(LocalDateTime at) {
        return !revoked && !at.isBefore(validFrom) && at.isBefore(validUntil);
    }
}
