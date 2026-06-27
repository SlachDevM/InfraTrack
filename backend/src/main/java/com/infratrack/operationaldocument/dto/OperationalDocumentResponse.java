package com.infratrack.operationaldocument.dto;

import com.infratrack.operationaldocument.OperationalDocument;
import com.infratrack.operationaldocument.OperationalDocumentOwnerType;
import com.infratrack.operationaldocument.OperationalDocumentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class OperationalDocumentResponse {

    private Long id;
    private Long assetId;
    private String assetName;
    private OperationalDocumentOwnerType ownerType;
    private Long ownerId;
    @Schema(description = "Business document classification")
    private OperationalDocumentType documentType;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    @Schema(description = "Business date associated with the document content")
    private LocalDate documentDate;
    private Long uploadedByUserId;
    private LocalDateTime uploadedAt;
    private Long createdAt;
    private Long updatedAt;

    public static OperationalDocumentResponse from(OperationalDocument document) {
        OperationalDocumentResponse response = new OperationalDocumentResponse();
        response.id = document.getId();
        response.assetId = document.getAsset().getId();
        response.assetName = document.getAsset().getName();
        response.ownerType = document.getOwnerType();
        response.ownerId = document.getOwnerId();
        response.documentType = document.getDocumentType();
        response.originalFileName = document.getOriginalFileName();
        response.contentType = document.getContentType();
        response.fileSize = document.getFileSize();
        response.documentDate = document.getDocumentDate();
        response.uploadedByUserId = document.getUploadedByUserId();
        response.uploadedAt = document.getUploadedAt();
        response.createdAt = document.getCreatedAt();
        response.updatedAt = document.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getAssetId() {
        return assetId;
    }

    public String getAssetName() {
        return assetName;
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

    public String getContentType() {
        return contentType;
    }

    public Long getFileSize() {
        return fileSize;
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
