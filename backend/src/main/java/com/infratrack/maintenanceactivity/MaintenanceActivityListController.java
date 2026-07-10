package com.infratrack.maintenanceactivity;

import com.infratrack.config.PaginationSupport;
import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.maintenanceactivity.dto.MaintenanceActivityResponse;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maintenance-activities")
@Tag(name = "Maintenance Activities", description = "Maintenance activity listing (UC-009)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class MaintenanceActivityListController {

    private final MaintenanceActivityService maintenanceActivityService;

    public MaintenanceActivityListController(MaintenanceActivityService maintenanceActivityService) {
        this.maintenanceActivityService = maintenanceActivityService;
    }

    @GetMapping
    @Operation(
            summary = "List maintenance activities",
            description = "Returns paginated maintenance activities ordered by completion time. "
                    + "When eligibleForCompletionReview is true, returns only activities "
                    + "eligible for manager completion review (UC-010).")
    @ApiResponse(responseCode = "200", description = "Paginated maintenance activity list")
    public ResponseEntity<Page<MaintenanceActivityResponse>> listMaintenanceActivities(
            @Parameter(description = "Zero-based page index") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (max 100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "When true, returns only maintenance activities eligible for completion review")
            @RequestParam(required = false) Boolean eligibleForCompletionReview,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "completedAt"));
        if (Boolean.TRUE.equals(eligibleForCompletionReview)) {
            return ResponseEntity.ok(
                    maintenanceActivityService.listEligibleForCompletionReviewPage(userId, pageable));
        }
        return ResponseEntity.ok(maintenanceActivityService.listPage(userId, pageable));
    }
}
