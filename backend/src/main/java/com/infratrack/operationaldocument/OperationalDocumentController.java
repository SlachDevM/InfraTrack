package com.infratrack.operationaldocument;

import com.infratrack.config.PaginationSupport;
import com.infratrack.operationaldocument.dto.OperationalDocumentResponse;
import com.infratrack.operationaldocument.dto.OperationalDocumentSummaryResponse;
import com.infratrack.security.JwtAuthenticationToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
public class OperationalDocumentController {

    private final OperationalDocumentService operationalDocumentService;

    public OperationalDocumentController(OperationalDocumentService operationalDocumentService) {
        this.operationalDocumentService = operationalDocumentService;
    }

    @PostMapping(value = "/api/assets/{assetId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OperationalDocumentResponse> uploadDocument(
            @PathVariable Long assetId,
            @RequestPart("file") MultipartFile file,
            @RequestPart("documentType") OperationalDocumentType documentType,
            @RequestPart(value = "ownerType", required = false) OperationalDocumentOwnerType ownerType,
            @RequestPart(value = "ownerId", required = false) Long ownerId,
            @RequestPart(value = "documentDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate documentDate,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        OperationalDocumentResponse response = operationalDocumentService.uploadDocument(
                assetId,
                file,
                documentType,
                ownerType,
                ownerId,
                documentDate,
                userId);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/api/assets/{assetId}/documents")
    public ResponseEntity<Page<OperationalDocumentSummaryResponse>> listDocuments(
            @PathVariable Long assetId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "uploadedAt"));
        return ResponseEntity.ok(operationalDocumentService.listDocuments(assetId, pageable));
    }

    @GetMapping("/api/operational-documents/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        OperationalDocumentService.OperationalDocumentDownload download =
                operationalDocumentService.downloadDocument(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + download.originalFileName() + "\"")
                .contentType(MediaType.parseMediaType(download.contentType()))
                .body(download.resource());
    }
}
