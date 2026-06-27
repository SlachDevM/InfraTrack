package com.infratrack.asset;

import com.infratrack.asset.dto.AssetResponse;
import com.infratrack.asset.dto.RegisterAssetRequest;
import com.infratrack.config.PaginationSupport;
import com.infratrack.security.JwtAuthenticationToken;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assets")
@CrossOrigin(origins = "http://localhost:3000")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    public ResponseEntity<?> listAssets(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (PaginationSupport.isUnpagedRequest(page, size)) {
            return ResponseEntity.ok(assetService.listAll());
        }
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "registrationDate"));
        return ResponseEntity.ok(assetService.listPage(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetResponse> getAsset(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.getById(id));
    }

    @PostMapping
    public ResponseEntity<AssetResponse> registerAsset(
            @Valid @RequestBody RegisterAssetRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        AssetResponse response = assetService.registerAsset(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
