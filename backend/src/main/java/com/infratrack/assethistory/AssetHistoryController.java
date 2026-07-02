package com.infratrack.assethistory;

import com.infratrack.config.PaginationSupport;
import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assets")
@Tag(name = "Asset History", description = "Permanent operational history for an asset (UC-011)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class AssetHistoryController {

    private final AssetHistoryService assetHistoryService;

    public AssetHistoryController(AssetHistoryService assetHistoryService) {
        this.assetHistoryService = assetHistoryService;
    }

    @GetMapping("/{id}/history")
    @Operation(
            summary = "Get asset history",
            description = "Returns paginated operational history events ordered by event date (UC-011).")
    @ApiResponse(responseCode = "200", description = "Paginated history events")
    public ResponseEntity<Page<AssetHistoryResponse>> getAssetHistory(
            @PathVariable Long id,
            @Parameter(description = "Zero-based page index") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (max 100)") @RequestParam(required = false) Integer size,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("eventDate"),
                        Sort.Order.desc("createdAt")));
        return ResponseEntity.ok(assetHistoryService.getAssetHistory(id, userId, pageable));
    }
}
