package com.infratrack.operationsintelligence;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.operationsintelligence.dto.RecentActivityResponse;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operations-intelligence")
@Tag(name = "Operations Intelligence", description = "Read-only operational KPI, trend, and activity aggregation (V2.1.0)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class OperationsRecentActivityController {

    private final OperationsRecentActivityService recentActivityService;

    public OperationsRecentActivityController(OperationsRecentActivityService recentActivityService) {
        this.recentActivityService = recentActivityService;
    }

    @GetMapping("/recent-activity")
    @Operation(summary = "Get recent operational activity")
    @ApiResponse(responseCode = "200", description = "Recent activity items for the caller's scope")
    public ResponseEntity<RecentActivityResponse> getRecentActivity(
            Authentication authentication,
            @RequestParam(required = false) Integer limit) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(recentActivityService.getRecentActivity(userId, limit));
    }
}
