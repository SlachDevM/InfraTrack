package com.infratrack.operationaldecision;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.operationaldecision.dto.CreateOperationalDecisionRequest;
import com.infratrack.operationaldecision.dto.OperationalDecisionResponse;
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
@RequestMapping("/api/operational-decisions")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Operational Decisions", description = "Manager operational decisions on issues (UC-007)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class OperationalDecisionController {

    private final OperationalDecisionService operationalDecisionService;

    public OperationalDecisionController(OperationalDecisionService operationalDecisionService) {
        this.operationalDecisionService = operationalDecisionService;
    }

    @GetMapping
    @Operation(summary = "List operational decisions")
    @ApiResponse(responseCode = "200", description = "Operational decision list")
    public ResponseEntity<List<OperationalDecisionResponse>> listOperationalDecisions() {
        return ResponseEntity.ok(operationalDecisionService.listAll());
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
