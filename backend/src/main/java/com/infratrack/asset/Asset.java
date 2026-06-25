package com.infratrack.asset;

import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.department.Department;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_category_id", nullable = false)
    private AssetCategory assetCategory;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @Column(name = "registered_by_user_id", nullable = false)
    private Long registeredByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    protected Asset() {
    }

    public Asset(
            String name,
            Department department,
            AssetCategory assetCategory,
            String location,
            AssetStatus status,
            LocalDate registrationDate,
            Long registeredByUserId) {
        this.name = name;
        this.department = department;
        this.assetCategory = assetCategory;
        this.location = location;
        this.status = status;
        this.registrationDate = registrationDate;
        this.registeredByUserId = registeredByUserId;
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

    public String getName() {
        return name;
    }

    public Department getDepartment() {
        return department;
    }

    public AssetCategory getAssetCategory() {
        return assetCategory;
    }

    public String getLocation() {
        return location;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public Long getRegisteredByUserId() {
        return registeredByUserId;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
