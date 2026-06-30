package com.infratrack.operationsintelligence;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.operationsintelligence.dto.OperationsKpiResponse;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operations-intelligence")
@Tag(name = "Operations Intelligence", description = "Read-only operational KPI aggregation (V2.1.0 Sprint C1)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class OperationsKpiController {

    private final OperationsKpiService kpiService;

    public OperationsKpiController(OperationsKpiService kpiService) {
        this.kpiService = kpiService;
    }

    @GetMapping("/kpis")
    @Operation(summary = "Get operational KPI aggregates")
    @ApiResponse(responseCode = "200", description = "Aggregated KPI values for the caller's scope")
    public ResponseEntity<OperationsKpiResponse> getKpis(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(kpiService.getKpis(userId));
    }
}
