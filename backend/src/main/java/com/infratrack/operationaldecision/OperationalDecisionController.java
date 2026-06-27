package com.infratrack.operationaldecision;

import com.infratrack.config.PaginationSupport;
import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.operationaldecision.dto.CreateOperationalDecisionRequest;
import com.infratrack.operationaldecision.dto.OperationalDecisionResponse;
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
@RequestMapping("/api/operational-decisions")
@Tag(name = "Operational Decisions", description = "Manager operational decisions on issues (UC-007)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class OperationalDecisionController {

    private final OperationalDecisionService operationalDecisionService;

    public OperationalDecisionController(OperationalDecisionService operationalDecisionService) {
        this.operationalDecisionService = operationalDecisionService;
    }

    @GetMapping
    @Operation(summary = "List operational decisions", description = "Returns paginated operational decisions ordered by creation time.")
    @ApiResponse(responseCode = "200", description = "Paginated operational decision list")
    public ResponseEntity<Page<OperationalDecisionResponse>> listOperationalDecisions(
            @Parameter(description = "Zero-based page index") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (max 100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "When true, returns only decisions eligible for work order creation (UC-008)")
            @RequestParam(required = false) Boolean eligibleForWorkOrderCreation,
            Authentication authentication) {
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        if (Boolean.TRUE.equals(eligibleForWorkOrderCreation)) {
            Long userId = ((JwtAuthenticationToken) authentication).getUserId();
            return ResponseEntity.ok(
                    operationalDecisionService.listEligibleForWorkOrderCreationPage(userId, pageable));
        }
        return ResponseEntity.ok(operationalDecisionService.listPage(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get operational decision by ID")
    @ApiResponse(responseCode = "200", description = "Operational decision details")
    public ResponseEntity<OperationalDecisionResponse> getOperationalDecision(@PathVariable Long id) {
        return ResponseEntity.ok(operationalDecisionService.getById(id));
    }

    @PostMapping
    @Operation(
            summary = "Make operational decision",
            description = "Records a manager decision outcome for an issue (UC-007).")
    @ApiResponse(responseCode = "201", description = "Operational decision recorded")
    public ResponseEntity<OperationalDecisionResponse> makeOperationalDecision(
            @Valid @RequestBody CreateOperationalDecisionRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        OperationalDecisionResponse response = operationalDecisionService.makeOperationalDecision(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
