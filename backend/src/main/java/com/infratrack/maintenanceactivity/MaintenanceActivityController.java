package com.infratrack.maintenanceactivity;

import com.infratrack.maintenanceactivity.dto.CompleteMaintenanceActivityRequest;
import com.infratrack.maintenanceactivity.dto.MaintenanceActivityResponse;
import com.infratrack.security.JwtAuthenticationToken;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/work-orders")
@CrossOrigin(origins = "http://localhost:3000")
public class MaintenanceActivityController {

    private final MaintenanceActivityService maintenanceActivityService;

    public MaintenanceActivityController(MaintenanceActivityService maintenanceActivityService) {
        this.maintenanceActivityService = maintenanceActivityService;
    }

    @PostMapping("/{id}/maintenance-activity")
    public ResponseEntity<MaintenanceActivityResponse> completeMaintenance(
            @PathVariable Long id,
            @Valid @RequestBody CompleteMaintenanceActivityRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        MaintenanceActivityResponse response = maintenanceActivityService.completeMaintenance(id, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
