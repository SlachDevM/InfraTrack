package com.infratrack.businesstrigger;

import com.infratrack.businesstrigger.dto.BusinessTriggerResponse;
import com.infratrack.businesstrigger.dto.CreateBusinessTriggerRequest;
import com.infratrack.security.JwtAuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business-triggers")
@CrossOrigin(origins = "http://localhost:3000")
public class BusinessTriggerController {

    private final BusinessTriggerService businessTriggerService;

    public BusinessTriggerController(BusinessTriggerService businessTriggerService) {
        this.businessTriggerService = businessTriggerService;
    }

    @GetMapping
    public ResponseEntity<List<BusinessTriggerResponse>> listBusinessTriggers() {
        return ResponseEntity.ok(businessTriggerService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessTriggerResponse> getBusinessTrigger(@PathVariable Long id) {
        return ResponseEntity.ok(businessTriggerService.getById(id));
    }

    @PostMapping
    public ResponseEntity<BusinessTriggerResponse> createBusinessTrigger(
            @RequestBody CreateBusinessTriggerRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        BusinessTriggerResponse response = businessTriggerService.createBusinessTrigger(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
