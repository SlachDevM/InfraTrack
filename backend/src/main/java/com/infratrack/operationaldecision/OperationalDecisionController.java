package com.infratrack.operationaldecision;

import com.infratrack.operationaldecision.dto.CreateOperationalDecisionRequest;
import com.infratrack.operationaldecision.dto.OperationalDecisionResponse;
import com.infratrack.security.JwtAuthenticationToken;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operational-decisions")
@CrossOrigin(origins = "http://localhost:3000")
public class OperationalDecisionController {

    private final OperationalDecisionService operationalDecisionService;

    public OperationalDecisionController(OperationalDecisionService operationalDecisionService) {
        this.operationalDecisionService = operationalDecisionService;
    }

    @GetMapping
    public ResponseEntity<List<OperationalDecisionResponse>> listOperationalDecisions() {
        return ResponseEntity.ok(operationalDecisionService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperationalDecisionResponse> getOperationalDecision(@PathVariable Long id) {
        return ResponseEntity.ok(operationalDecisionService.getById(id));
    }

    @PostMapping
    public ResponseEntity<OperationalDecisionResponse> makeOperationalDecision(
            @Valid @RequestBody CreateOperationalDecisionRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        OperationalDecisionResponse response = operationalDecisionService.makeOperationalDecision(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
