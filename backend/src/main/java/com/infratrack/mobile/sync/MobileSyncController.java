package com.infratrack.mobile.sync;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.mobile.sync.dto.SyncRequest;
import com.infratrack.mobile.sync.dto.SyncResponse;
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
@RequestMapping("/api/mobile")
@Tag(name = "Mobile API", description = "Compact read/bundle endpoints for the Android field client")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class MobileSyncController {

    private final MobileSyncService mobileSyncService;

    public MobileSyncController(MobileSyncService mobileSyncService) {
        this.mobileSyncService = mobileSyncService;
    }

    @PostMapping("/sync")
    @Operation(
            summary = "Mobile offline synchronization handshake",
            description = "Protocol foundation for Android offline sync (M5.2-BE1). Accepts pending "
                    + "operations structurally but does not apply them yet. Returns an empty sync envelope.")
    @ApiResponse(responseCode = "200", description = "Sync protocol response (no domain mutations in M5.2-BE1)")
    public ResponseEntity<SyncResponse> sync(
            Authentication authentication,
            @Valid @RequestBody SyncRequest request) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(mobileSyncService.sync(userId, request));
    }
}
