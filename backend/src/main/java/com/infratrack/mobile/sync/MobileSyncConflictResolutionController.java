package com.infratrack.mobile.sync;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.mobile.sync.dto.SyncConflictResolutionRequest;
import com.infratrack.mobile.sync.dto.SyncConflictResolutionResponse;
import com.infratrack.observability.MobileEndpointMetricsRecorder;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile/sync/conflicts")
@Tag(name = "Mobile API", description = "Compact read/bundle endpoints for the Android field client")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class MobileSyncConflictResolutionController {

    private final SyncConflictResolutionService conflictResolutionService;
    private final MobileEndpointMetricsRecorder endpointMetrics;

    public MobileSyncConflictResolutionController(
            SyncConflictResolutionService conflictResolutionService,
            MobileEndpointMetricsRecorder endpointMetrics) {
        this.conflictResolutionService = conflictResolutionService;
        this.endpointMetrics = endpointMetrics;
    }

    @PostMapping("/resolve")
    @Operation(
            summary = "Resolve a mobile sync conflict explicitly",
            description = "Records an explicit conflict resolution decision for SAVE_INSPECTION_PROGRESS. "
                    + "Does not apply client payloads or mutate server workflow state (M5.5-BE2).")
    @ApiResponse(responseCode = "200", description = "Resolution outcome recorded for the client")
    public ResponseEntity<SyncConflictResolutionResponse> resolve(
            Authentication authentication,
            @Valid @RequestBody SyncConflictResolutionRequest request) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(endpointMetrics.recordSyncConflictResolve(
                () -> conflictResolutionService.resolve(userId, request)));
    }
}
