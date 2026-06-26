package com.infratrack.delegatedauthority;

import com.infratrack.delegatedauthority.dto.CreateDelegatedAuthorityRequest;
import com.infratrack.delegatedauthority.dto.DelegatedAuthorityResponse;
import com.infratrack.security.JwtAuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delegated-authorities")
@CrossOrigin(origins = "http://localhost:3000")
public class DelegatedAuthorityController {

    private final DelegatedAuthorityService delegatedAuthorityService;

    public DelegatedAuthorityController(DelegatedAuthorityService delegatedAuthorityService) {
        this.delegatedAuthorityService = delegatedAuthorityService;
    }

    @GetMapping
    public ResponseEntity<List<DelegatedAuthorityResponse>> listDelegatedAuthorities() {
        return ResponseEntity.ok(delegatedAuthorityService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DelegatedAuthorityResponse> getDelegatedAuthority(@PathVariable Long id) {
        return ResponseEntity.ok(delegatedAuthorityService.getById(id));
    }

    @PostMapping
    public ResponseEntity<DelegatedAuthorityResponse> createDelegatedAuthority(
            @RequestBody CreateDelegatedAuthorityRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(delegatedAuthorityService.create(request, userId));
    }

    @PostMapping("/{id}/revoke")
    public ResponseEntity<DelegatedAuthorityResponse> revokeDelegatedAuthority(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(delegatedAuthorityService.revoke(id, userId));
    }
}
