package com.infratrack.inspection;

import com.infratrack.inspection.dto.AssignInspectionRequest;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.security.JwtAuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inspections")
@CrossOrigin(origins = "http://localhost:3000")
public class InspectionController {

    private final InspectionService inspectionService;

    public InspectionController(InspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    @GetMapping
    public ResponseEntity<List<InspectionResponse>> listInspections() {
        return ResponseEntity.ok(inspectionService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InspectionResponse> getInspection(@PathVariable Long id) {
        return ResponseEntity.ok(inspectionService.getById(id));
    }

    @PostMapping
    public ResponseEntity<InspectionResponse> assignInspection(
            @RequestBody AssignInspectionRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        InspectionResponse response = inspectionService.assignInspection(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
