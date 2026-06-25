package com.infratrack.operationaldocument;

import com.infratrack.asset.Asset;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "operational_documents")
public class OperationalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    private OperationalDocumentOwnerType ownerType;

    @Column(name = "owner_id")
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private OperationalDocumentType documentType;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false)
    private String storedFileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "document_date")
    private LocalDate documentDate;

    @Column(name = "uploaded_by_user_id", nullable = false)
    private Long uploadedByUserId;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    protected OperationalDocument() {
    }

    public OperationalDocument(
            Asset asset,
            OperationalDocumentOwnerType ownerType,
            Long ownerId,
            OperationalDocumentType documentType,
            String originalFileName,
            String storedFileName,
            String contentType,
            Long fileSize,
            String storagePath,
            LocalDate documentDate,
            Long uploadedByUserId,
            LocalDateTime uploadedAt) {
        this.asset = asset;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.documentType = documentType;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.storagePath = storagePath;
        this.documentDate = documentDate;
        this.uploadedByUserId = uploadedByUserId;
        this.uploadedAt = uploadedAt;
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

    public Asset getAsset() {
        return asset;
    }

    public OperationalDocumentOwnerType getOwnerType() {
        return ownerType;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public OperationalDocumentType getDocumentType() {
        return documentType;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public LocalDate getDocumentDate() {
        return documentDate;
    }

    public Long getUploadedByUserId() {
        return uploadedByUserId;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
