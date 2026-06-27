package com.infratrack.issue;

import com.infratrack.issue.dto.CreateIssueRequest;
import com.infratrack.issue.dto.IssueResponse;
import com.infratrack.security.JwtAuthenticationToken;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "http://localhost:3000")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping
    public ResponseEntity<List<IssueResponse>> listIssues() {
        return ResponseEntity.ok(issueService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueResponse> getIssue(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.getById(id));
    }

    @PostMapping
    public ResponseEntity<IssueResponse> recordIssue(
            @Valid @RequestBody CreateIssueRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        IssueResponse response = issueService.recordIssue(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
