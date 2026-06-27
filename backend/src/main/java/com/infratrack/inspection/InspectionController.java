package com.infratrack.inspection;

import com.infratrack.config.PaginationSupport;
import com.infratrack.inspection.dto.AssignInspectionRequest;
import com.infratrack.inspection.dto.CompleteInspectionRequest;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.security.JwtAuthenticationToken;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inspections")
@CrossOrigin(origins = "http://localhost:3000")
public class InspectionController {

    private final InspectionService inspectionService;

    public InspectionController(InspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    @GetMapping
    public ResponseEntity<?> listInspections(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (PaginationSupport.isUnpagedRequest(page, size)) {
            return ResponseEntity.ok(inspectionService.listAll());
        }
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(inspectionService.listPage(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InspectionResponse> getInspection(@PathVariable Long id) {
        return ResponseEntity.ok(inspectionService.getById(id));
    }

    @PostMapping
    public ResponseEntity<InspectionResponse> assignInspection(
            @Valid @RequestBody AssignInspectionRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        InspectionResponse response = inspectionService.assignInspection(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<InspectionResponse> completeInspection(
            @PathVariable Long id,
            @Valid @RequestBody CompleteInspectionRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        InspectionResponse response = inspectionService.completeInspection(id, request, userId);
        return ResponseEntity.ok(response);
    }
}
