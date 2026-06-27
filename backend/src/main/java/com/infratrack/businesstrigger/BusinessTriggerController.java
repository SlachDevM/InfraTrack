package com.infratrack.businesstrigger;

import com.infratrack.businesstrigger.dto.BusinessTriggerResponse;
import com.infratrack.businesstrigger.dto.CreateBusinessTriggerRequest;
import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/business-triggers")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Business Triggers", description = "Operational business trigger recording (UC-006)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class BusinessTriggerController {

    private final BusinessTriggerService businessTriggerService;

    public BusinessTriggerController(BusinessTriggerService businessTriggerService) {
        this.businessTriggerService = businessTriggerService;
    }

    @GetMapping
    @Operation(summary = "List business triggers")
    @ApiResponse(responseCode = "200", description = "Business trigger list")
    public ResponseEntity<List<BusinessTriggerResponse>> listBusinessTriggers() {
        return ResponseEntity.ok(businessTriggerService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get business trigger by ID")
    @ApiResponse(responseCode = "200", description = "Business trigger details")
    public ResponseEntity<BusinessTriggerResponse> getBusinessTrigger(@PathVariable Long id) {
        return ResponseEntity.ok(businessTriggerService.getById(id));
    }

    @PostMapping
    @Operation(
            summary = "Create business trigger",
            description = "Records an operational business trigger against an asset (UC-006).")
    @ApiResponse(responseCode = "201", description = "Business trigger created")
    public ResponseEntity<BusinessTriggerResponse> createBusinessTrigger(
            @Valid @RequestBody CreateBusinessTriggerRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        BusinessTriggerResponse response = businessTriggerService.createBusinessTrigger(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
