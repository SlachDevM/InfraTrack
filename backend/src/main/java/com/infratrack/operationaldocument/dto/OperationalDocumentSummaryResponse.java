package com.infratrack.operationaldocument.dto;

import com.infratrack.operationaldocument.OperationalDocument;
import com.infratrack.operationaldocument.OperationalDocumentOwnerType;
import com.infratrack.operationaldocument.OperationalDocumentType;

import java.time.LocalDateTime;

public class OperationalDocumentSummaryResponse {

    private Long id;
    private OperationalDocumentOwnerType ownerType;
    private Long ownerId;
    private OperationalDocumentType documentType;
    private String originalFileName;
    private LocalDateTime uploadedAt;

    public static OperationalDocumentSummaryResponse from(OperationalDocument document) {
        OperationalDocumentSummaryResponse response = new OperationalDocumentSummaryResponse();
        response.id = document.getId();
        response.ownerType = document.getOwnerType();
        response.ownerId = document.getOwnerId();
        response.documentType = document.getDocumentType();
        response.originalFileName = document.getOriginalFileName();
        response.uploadedAt = document.getUploadedAt();
        return response;
    }

    public Long getId() {
        return id;
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

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}
