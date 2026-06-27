package com.infratrack.assetcategory;

import com.infratrack.assetcategory.dto.AssetCategoryResponse;
import com.infratrack.assetcategory.dto.CreateAssetCategoryRequest;
import com.infratrack.assetcategory.dto.UpdateAssetCategoryRequest;
import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.security.JwtAuthenticationToken;
import com.infratrack.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/asset-categories")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Asset Categories", description = "Asset category reference data")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class AssetCategoryController {

    private final AssetCategoryService assetCategoryService;
    private final UserService userService;

    public AssetCategoryController(AssetCategoryService assetCategoryService, UserService userService) {
        this.assetCategoryService = assetCategoryService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List asset categories")
    @ApiResponse(responseCode = "200", description = "Asset category list")
    public ResponseEntity<List<AssetCategoryResponse>> listAssetCategories() {
        return ResponseEntity.ok(assetCategoryService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get asset category by ID")
    @ApiResponse(responseCode = "200", description = "Asset category details")
    public ResponseEntity<AssetCategoryResponse> getAssetCategory(@PathVariable Long id) {
        return ResponseEntity.ok(assetCategoryService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create asset category", description = "Administrator only.")
    @ApiResponse(responseCode = "201", description = "Asset category created")
    public ResponseEntity<AssetCategoryResponse> createAssetCategory(
            @Valid @RequestBody CreateAssetCategoryRequest request,
            Authentication authentication) {
        requireAdministrator(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(assetCategoryService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update asset category", description = "Administrator only.")
    @ApiResponse(responseCode = "200", description = "Asset category updated")
    public ResponseEntity<AssetCategoryResponse> updateAssetCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssetCategoryRequest request,
            Authentication authentication) {
        requireAdministrator(authentication);
        return ResponseEntity.ok(assetCategoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete asset category", description = "Administrator only.")
    @ApiResponse(responseCode = "204", description = "Asset category deleted")
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
