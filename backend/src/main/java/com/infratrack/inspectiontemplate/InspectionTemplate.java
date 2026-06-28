package com.infratrack.inspectiontemplate;

import com.infratrack.assetcategory.AssetCategory;
import jakarta.persistence.*;

@Entity
@Table(name = "inspection_templates")
public class InspectionTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_category_id", nullable = false)
    private AssetCategory assetCategory;

    @Column(nullable = false)
    private Integer version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionTemplateStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    protected InspectionTemplate() {
    }

    public InspectionTemplate(
            String name,
            String description,
            AssetCategory assetCategory,
            Integer version,
            InspectionTemplateStatus status) {
        this.name = name;
        this.description = description;
        this.assetCategory = assetCategory;
        this.version = version;
        this.status = status;
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

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AssetCategory getAssetCategory() {
        return assetCategory;
    }

    public Integer getVersion() {
        return version;
    }

    public InspectionTemplateStatus getStatus() {
        return status;
    }

    public void setStatus(InspectionTemplateStatus status) {
        this.status = status;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void touchUpdatedAt() {
        this.updatedAt = System.currentTimeMillis();
    }
}
