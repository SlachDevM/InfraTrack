package com.infratrack.assetcategory;

import com.infratrack.assetcategory.dto.AssetCategoryResponse;
import com.infratrack.assetcategory.dto.CreateAssetCategoryRequest;
import com.infratrack.assetcategory.dto.UpdateAssetCategoryRequest;
import com.infratrack.security.JwtAuthenticationToken;
import com.infratrack.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/asset-categories")
@CrossOrigin(origins = "http://localhost:3000")
public class AssetCategoryController {

    private final AssetCategoryService assetCategoryService;
    private final UserService userService;

    public AssetCategoryController(AssetCategoryService assetCategoryService, UserService userService) {
        this.assetCategoryService = assetCategoryService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<AssetCategoryResponse>> listAssetCategories() {
        return ResponseEntity.ok(assetCategoryService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetCategoryResponse> getAssetCategory(@PathVariable Long id) {
        return ResponseEntity.ok(assetCategoryService.getById(id));
    }

    @PostMapping
    public ResponseEntity<AssetCategoryResponse> createAssetCategory(
            @RequestBody CreateAssetCategoryRequest request,
            Authentication authentication) {
        requireAdministrator(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(assetCategoryService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetCategoryResponse> updateAssetCategory(
            @PathVariable Long id,
            @RequestBody UpdateAssetCategoryRequest request,
            Authentication authentication) {
        requireAdministrator(authentication);
        return ResponseEntity.ok(assetCategoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssetCategory(
            @PathVariable Long id,
            Authentication authentication) {
        requireAdministrator(authentication);
        assetCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void requireAdministrator(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        if (!userService.isAdministrator(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only administrators can manage asset categories");
        }
    }
}
