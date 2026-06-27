package com.infratrack.maintenanceactivity;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.maintenanceactivity.dto.CompleteMaintenanceActivityRequest;
import com.infratrack.maintenanceactivity.dto.MaintenanceActivityResponse;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/work-orders")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Maintenance Activities", description = "Maintenance completion on work orders (UC-009)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class MaintenanceActivityController {

    private final MaintenanceActivityService maintenanceActivityService;

    public MaintenanceActivityController(MaintenanceActivityService maintenanceActivityService) {
        this.maintenanceActivityService = maintenanceActivityService;
    }

    @PostMapping("/{id}/maintenance-activity")
    @Operation(
            summary = "Complete maintenance activity",
            description = "Records maintenance performed against an assigned work order (UC-009).")
    @ApiResponse(responseCode = "201", description = "Maintenance activity recorded")
    public ResponseEntity<MaintenanceActivityResponse> completeMaintenance(
            @PathVariable Long id,
            @Valid @RequestBody CompleteMaintenanceActivityRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        MaintenanceActivityResponse response = maintenanceActivityService.completeMaintenance(id, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
