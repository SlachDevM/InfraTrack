package com.infratrack.operationsintelligence;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.operationsintelligence.dto.OperationsTrendResponse;
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
@Tag(name = "Operations Intelligence", description = "Read-only operational KPI and trend aggregation (V2.1.0)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class OperationsTrendController {

    private final OperationsTrendService trendService;

    public OperationsTrendController(OperationsTrendService trendService) {
        this.trendService = trendService;
    }

    @GetMapping("/trends")
    @Operation(summary = "Get operational trend time-series")
    @ApiResponse(responseCode = "200", description = "Trend series for the caller's scope")
    public ResponseEntity<OperationsTrendResponse> getTrends(
            Authentication authentication,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to,
            @RequestParam(required = false) String bucket) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(trendService.getTrends(userId, from, to, bucket));
    }
}
