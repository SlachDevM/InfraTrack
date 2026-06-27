package com.infratrack.asset;

import com.infratrack.asset.dto.AssetResponse;
import com.infratrack.asset.dto.AssetSummaryResponse;
import com.infratrack.asset.dto.RegisterAssetRequest;
import com.infratrack.config.PaginationSupport;
import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assets")
@Tag(name = "Assets", description = "Asset registration and listing (UC-001)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    @Operation(
            summary = "List assets",
            description = "Returns a paginated list of registered assets ordered by registration date (newest first).")
    @ApiResponse(responseCode = "200", description = "Paginated asset summaries")
    public ResponseEntity<Page<AssetSummaryResponse>> listAssets(
            @Parameter(description = "Zero-based page index") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (max 100)") @RequestParam(required = false) Integer size) {
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "registrationDate"));
        return ResponseEntity.ok(assetService.listPage(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get asset by ID", description = "Returns full asset details.")
    @ApiResponse(responseCode = "200", description = "Asset details")
    public ResponseEntity<AssetResponse> getAsset(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.getById(id));
    }

    @PostMapping
    @Operation(
            summary = "Register asset",
            description = "Registers a new asset and records the ASSET_REGISTERED history event (UC-001). "
                    + "Requires Manager or Operational Coordinator role.")
    @ApiResponse(responseCode = "201", description = "Asset registered")
    public ResponseEntity<AssetResponse> registerAsset(
            @Valid @RequestBody RegisterAssetRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        AssetResponse response = assetService.registerAsset(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
