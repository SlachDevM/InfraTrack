package com.infratrack.mobile.dto;

import com.infratrack.operationaldocument.OperationalDocument;
import com.infratrack.operationaldocument.OperationalDocumentOwnerType;

import java.time.LocalDateTime;

public class MobileAssetDocumentSummaryResponse {

    private Long id;
    private String filename;
    private String contentType;
    private OperationalDocumentOwnerType ownerType;
    private LocalDateTime uploadedAt;
    private String uploadedBy;
    private String downloadUrl;

    public static MobileAssetDocumentSummaryResponse from(OperationalDocument document, String uploadedByName) {
        MobileAssetDocumentSummaryResponse response = new MobileAssetDocumentSummaryResponse();
        response.id = document.getId();
        response.filename = document.getOriginalFileName();
        response.contentType = document.getContentType();
        response.ownerType = document.getOwnerType();
        response.uploadedAt = document.getUploadedAt();
        response.uploadedBy = uploadedByName;
        response.downloadUrl = "/api/operational-documents/" + document.getId() + "/download";
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public OperationalDocumentOwnerType getOwnerType() {
        return ownerType;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}
