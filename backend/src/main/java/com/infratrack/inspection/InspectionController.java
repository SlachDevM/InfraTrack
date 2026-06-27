package com.infratrack.inspection;

import com.infratrack.config.PaginationSupport;
import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.inspection.dto.AssignInspectionRequest;
import com.infratrack.inspection.dto.CompleteInspectionRequest;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.inspection.dto.InspectionSummaryResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inspections")
@Tag(name = "Inspections", description = "Inspection assignment and completion (UC-003, UC-004)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class InspectionController {

    private final InspectionService inspectionService;

    public InspectionController(InspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    @GetMapping
    @Operation(summary = "List inspections", description = "Returns paginated inspection summaries ordered by creation time.")
    @ApiResponse(responseCode = "200", description = "Paginated inspection summaries")
    public ResponseEntity<Page<InspectionSummaryResponse>> listInspections(
            @Parameter(description = "Zero-based page index") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (max 100)") @RequestParam(required = false) Integer size) {
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(inspectionService.listPage(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inspection by ID")
    @ApiResponse(responseCode = "200", description = "Inspection details")
    public ResponseEntity<InspectionResponse> getInspection(@PathVariable Long id) {
        return ResponseEntity.ok(inspectionService.getById(id));
    }

    @PostMapping
    @Operation(
            summary = "Assign inspection",
            description = "Assigns an inspection to a field employee or contractor (UC-003). Requires Operational Coordinator.")
    @ApiResponse(responseCode = "201", description = "Inspection assigned")
    public ResponseEntity<InspectionResponse> assignInspection(
            @Valid @RequestBody AssignInspectionRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        InspectionResponse response = inspectionService.assignInspection(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/complete")
    @Operation(
            summary = "Complete inspection",
            description = "Records field inspection results including physical condition and issue identification (UC-004).")
    @ApiResponse(responseCode = "200", description = "Inspection completed")
    public ResponseEntity<InspectionResponse> completeInspection(
            @PathVariable Long id,
            @Valid @RequestBody CompleteInspectionRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        InspectionResponse response = inspectionService.completeInspection(id, request, userId);
        return ResponseEntity.ok(response);
    }
}
