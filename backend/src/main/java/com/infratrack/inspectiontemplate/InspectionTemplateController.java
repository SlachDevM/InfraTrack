package com.infratrack.inspectiontemplate;

import com.infratrack.config.PaginationSupport;
import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.inspectiontemplate.dto.CreateInspectionTemplateRequest;
import com.infratrack.inspectiontemplate.dto.InspectionTemplateResponse;
import com.infratrack.inspectiontemplate.dto.UpdateInspectionTemplateRequest;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inspection-templates")
@Tag(name = "Inspection Templates", description = "Reusable inspection knowledge by asset category (V2 Domain Engine)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class InspectionTemplateController {

    private final InspectionTemplateService inspectionTemplateService;
    private final InspectionTemplateAuthorizationService authorizationService;

    public InspectionTemplateController(
            InspectionTemplateService inspectionTemplateService,
            InspectionTemplateAuthorizationService authorizationService) {
        this.inspectionTemplateService = inspectionTemplateService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    @Operation(summary = "List inspection templates", description = "Paginated list with optional category and status filters.")
    @ApiResponse(responseCode = "200", description = "Paginated inspection template list")
    public ResponseEntity<Page<InspectionTemplateResponse>> listInspectionTemplates(
            @Parameter(description = "Zero-based page index") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (max 100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Filter by asset category ID") @RequestParam(required = false) Long assetCategoryId,
            @Parameter(description = "Filter by template status") @RequestParam(required = false) InspectionTemplateStatus status,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireCanViewTemplates(userId);
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(inspectionTemplateService.listPage(assetCategoryId, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inspection template by ID")
    @ApiResponse(responseCode = "200", description = "Inspection template details")
    public ResponseEntity<InspectionTemplateResponse> getInspectionTemplate(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireCanViewTemplates(userId);
        return ResponseEntity.ok(inspectionTemplateService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create inspection template", description = "Administrator only. Initial version is 1 and status is DRAFT.")
    @ApiResponse(responseCode = "201", description = "Inspection template created")
    public ResponseEntity<InspectionTemplateResponse> createInspectionTemplate(
            @Valid @RequestBody CreateInspectionTemplateRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireAdministrator(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inspectionTemplateService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update inspection template metadata", description = "Administrator only.")
    @ApiResponse(responseCode = "200", description = "Inspection template updated")
    public ResponseEntity<InspectionTemplateResponse> updateInspectionTemplate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInspectionTemplateRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireAdministrator(userId);
        return ResponseEntity.ok(inspectionTemplateService.update(id, request));
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive inspection template", description = "Administrator only. Sets status to ARCHIVED.")
    @ApiResponse(responseCode = "200", description = "Inspection template archived")
    public ResponseEntity<InspectionTemplateResponse> archiveInspectionTemplate(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireAdministrator(userId);
        return ResponseEntity.ok(inspectionTemplateService.archive(id));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish inspection template",
            description = "Administrator only. Transitions status from DRAFT to PUBLISHED. Template must have at least one active question.")
    @ApiResponse(responseCode = "200", description = "Inspection template published")
    public ResponseEntity<InspectionTemplateResponse> publishInspectionTemplate(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireAdministrator(userId);
        return ResponseEntity.ok(inspectionTemplateService.publish(id));
    }
}
