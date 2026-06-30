package com.infratrack.operationsintelligence.dashboard;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.operationsintelligence.dashboard.dto.DashboardPreferencesRequest;
import com.infratrack.operationsintelligence.dashboard.dto.DashboardPreferencesResponse;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard/preferences")
@Tag(name = "Dashboard Preferences", description = "User-scoped Operations Intelligence dashboard presentation preferences (V2.1.0 Sprint C5)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class DashboardPreferencesController {

    private final DashboardPreferencesService preferencesService;

    public DashboardPreferencesController(DashboardPreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    @GetMapping
    @Operation(summary = "Get dashboard preferences for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Dashboard presentation preferences")
    public ResponseEntity<DashboardPreferencesResponse> getPreferences(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(preferencesService.getPreferences(userId));
    }

    @PutMapping
    @Operation(summary = "Save dashboard preferences for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Updated dashboard presentation preferences")
    public ResponseEntity<DashboardPreferencesResponse> savePreferences(
            Authentication authentication,
            @RequestBody DashboardPreferencesRequest request) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(preferencesService.savePreferences(userId, request));
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset dashboard preferences to defaults")
    @ApiResponse(responseCode = "200", description = "Default dashboard presentation preferences")
    public ResponseEntity<DashboardPreferencesResponse> resetPreferences(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(preferencesService.resetPreferences(userId));
    }
}
