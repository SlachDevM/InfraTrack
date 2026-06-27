package com.infratrack.issue;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.issue.dto.CreateIssueRequest;
import com.infratrack.issue.dto.IssueResponse;
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
@RequestMapping("/api/issues")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Issues", description = "Issue recording from completed inspections (UC-005)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping
    @Operation(summary = "List issues", description = "Returns all recorded issues.")
    @ApiResponse(responseCode = "200", description = "Issue list")
    public ResponseEntity<List<IssueResponse>> listIssues() {
        return ResponseEntity.ok(issueService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get issue by ID")
    @ApiResponse(responseCode = "200", description = "Issue details")
    public ResponseEntity<IssueResponse> getIssue(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.getById(id));
    }

    @PostMapping
    @Operation(
            summary = "Record issue",
            description = "Records an issue from a completed inspection (UC-005). At most one issue per inspection.")
    @ApiResponse(responseCode = "201", description = "Issue recorded")
    @ApiResponse(responseCode = "409", description = "Issue already recorded for this inspection")
    public ResponseEntity<IssueResponse> recordIssue(
            @Valid @RequestBody CreateIssueRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        IssueResponse response = issueService.recordIssue(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
