package com.infratrack.maintenanceactivity;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.maintenanceactivity.dto.MaintenanceActivityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance-activities")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Maintenance Activities", description = "Maintenance activity listing (UC-009)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class MaintenanceActivityListController {

    private final MaintenanceActivityService maintenanceActivityService;

    public MaintenanceActivityListController(MaintenanceActivityService maintenanceActivityService) {
        this.maintenanceActivityService = maintenanceActivityService;
    }

    @GetMapping
    @Operation(summary = "List maintenance activities", description = "Returns all recorded maintenance activities.")
    @ApiResponse(responseCode = "200", description = "Maintenance activity list")
    public ResponseEntity<List<MaintenanceActivityResponse>> listMaintenanceActivities() {
        return ResponseEntity.ok(maintenanceActivityService.listAll());
    }
}
