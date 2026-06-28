package com.infratrack.operationaldocument;

import com.infratrack.config.PaginationSupport;
import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.operationaldocument.dto.OperationalDocumentEligibleOwnerResponse;
import com.infratrack.operationaldocument.dto.OperationalDocumentResponse;
import com.infratrack.operationaldocument.dto.OperationalDocumentSummaryResponse;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@Tag(name = "Operational Documents", description = "Operational document upload, listing and download (UC-012)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class OperationalDocumentController {

    private final OperationalDocumentService operationalDocumentService;

    public OperationalDocumentController(OperationalDocumentService operationalDocumentService) {
        this.operationalDocumentService = operationalDocumentService;
    }

    @GetMapping("/api/assets/{assetId}/documents")
    @Operation(
            summary = "List operational documents for an asset",
            description = "Returns paginated document summaries for the asset. "
                    + "When eligibleOwners is true, returns owners eligible for document upload for the given ownerType.")
    @ApiResponse(responseCode = "200", description = "Paginated document summaries or eligible owners")
    public ResponseEntity<?> listDocuments(
            @PathVariable Long assetId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @Parameter(description = "When true, returns eligible operational owners for document upload")
            @RequestParam(required = false) Boolean eligibleOwners,
            @Parameter(description = "Required when eligibleOwners is true")
            @RequestParam(required = false) OperationalDocumentOwnerType ownerType,
            Authentication authentication) {
        if (Boolean.TRUE.equals(eligibleOwners)) {
            Long userId = ((JwtAuthenticationToken) authentication).getUserId();
            return ResponseEntity.ok(operationalDocumentService.listEligibleOwners(assetId, ownerType, userId));
        }
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "uploadedAt"));
        return ResponseEntity.ok(operationalDocumentService.listDocuments(assetId, pageable));
    }

    @PostMapping(value = "/api/assets/{assetId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload operational document",
            description = "Uploads PDF, PNG, JPEG, DOCX, or XLSX evidence linked to an asset or operational owner (UC-012). "
                    + "File content is validated server-side; maximum size 10 MB.")
    @ApiResponse(responseCode = "201", description = "Document uploaded")
    @ApiResponse(responseCode = "400", description = "Invalid file, filename, or document type")
    public ResponseEntity<OperationalDocumentResponse> uploadDocument(
            @PathVariable Long assetId,
            @Parameter(description = "PDF, PNG, JPEG, DOCX, or XLSX file") @RequestPart("file") MultipartFile file,
            @Parameter(description = "Business document type") @RequestParam("documentType") OperationalDocumentType documentType,
            @RequestParam(value = "ownerType", required = false) OperationalDocumentOwnerType ownerType,
            @RequestParam(value = "ownerId", required = false) Long ownerId,
            @RequestParam(value = "documentDate", required = false)
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
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/operational-documents/{id}/download")
    @Operation(summary = "Download operational document", description = "Returns the stored file as an attachment.")
    @ApiResponse(responseCode = "200", description = "File download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        OperationalDocumentService.OperationalDocumentDownload download =
                operationalDocumentService.downloadDocument(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + download.originalFileName() + "\"")
                .contentType(MediaType.parseMediaType(download.contentType()))
                .body(download.resource());
    }

    @DeleteMapping("/api/operational-documents/{id}")
    @Operation(summary = "Delete operational document", description = "Removes the stored document and metadata.")
    @ApiResponse(responseCode = "204", description = "Document deleted")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id, Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        operationalDocumentService.deleteDocument(id, userId);
        return ResponseEntity.noContent().build();
    }
}
