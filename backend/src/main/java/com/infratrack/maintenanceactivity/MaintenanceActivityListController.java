package com.infratrack.maintenanceactivity;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.maintenanceactivity.dto.MaintenanceActivityResponse;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
            description = "Returns recorded maintenance activities. "
                    + "When eligibleForCompletionReview is true, returns only activities "
                    + "eligible for manager completion review (UC-010).")
    @ApiResponse(responseCode = "200", description = "Maintenance activity list")
    public ResponseEntity<List<MaintenanceActivityResponse>> listMaintenanceActivities(
            @Parameter(description = "When true, returns only maintenance activities eligible for completion review")
            @RequestParam(required = false) Boolean eligibleForCompletionReview,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        if (Boolean.TRUE.equals(eligibleForCompletionReview)) {
            return ResponseEntity.ok(maintenanceActivityService.listEligibleForCompletionReview(userId));
        }
        return ResponseEntity.ok(maintenanceActivityService.listForUser(userId));
    }
}
