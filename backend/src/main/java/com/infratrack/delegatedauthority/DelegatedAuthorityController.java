package com.infratrack.delegatedauthority;

import com.infratrack.config.PaginationSupport;
import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.delegatedauthority.dto.CreateDelegatedAuthorityRequest;
import com.infratrack.delegatedauthority.dto.DelegatedAuthorityResponse;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/delegated-authorities")
@Tag(name = "Delegated Authorities", description = "Temporary delegation of manager authority (UC-008)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class DelegatedAuthorityController {

    private final DelegatedAuthorityService delegatedAuthorityService;

    public DelegatedAuthorityController(DelegatedAuthorityService delegatedAuthorityService) {
        this.delegatedAuthorityService = delegatedAuthorityService;
    }

    @GetMapping
    @Operation(summary = "List delegated authorities", description = "Returns paginated delegated authorities ordered by creation time.")
    @ApiResponse(responseCode = "200", description = "Paginated delegated authority list")
    public ResponseEntity<Page<DelegatedAuthorityResponse>> listDelegatedAuthorities(
            @Parameter(description = "Zero-based page index") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (max 100)") @RequestParam(required = false) Integer size) {
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(delegatedAuthorityService.listPage(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get delegated authority by ID")
    @ApiResponse(responseCode = "200", description = "Delegated authority details")
    public ResponseEntity<DelegatedAuthorityResponse> getDelegatedAuthority(@PathVariable Long id) {
        return ResponseEntity.ok(delegatedAuthorityService.getById(id));
    }

    @PostMapping
    @Operation(
            summary = "Create delegated authority",
            description = "Grants temporary manager authority to another user for a date range (UC-008).")
    @ApiResponse(responseCode = "201", description = "Delegated authority created")
    public ResponseEntity<DelegatedAuthorityResponse> createDelegatedAuthority(
            @Valid @RequestBody CreateDelegatedAuthorityRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(delegatedAuthorityService.create(request, userId));
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "Revoke delegated authority", description = "Ends an active delegation early.")
    @ApiResponse(responseCode = "200", description = "Delegated authority revoked")
    public ResponseEntity<DelegatedAuthorityResponse> revokeDelegatedAuthority(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(delegatedAuthorityService.revoke(id, userId));
    }
}
