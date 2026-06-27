package com.infratrack.workorder;

import com.infratrack.config.PaginationSupport;
import com.infratrack.security.JwtAuthenticationToken;
import com.infratrack.workorder.dto.AssignWorkOrderRequest;
import com.infratrack.workorder.dto.CreateWorkOrderRequest;
import com.infratrack.workorder.dto.WorkOrderResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/work-orders")
@CrossOrigin(origins = "http://localhost:3000")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @GetMapping
    public ResponseEntity<?> listWorkOrders(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (PaginationSupport.isUnpagedRequest(page, size)) {
            return ResponseEntity.ok(workOrderService.listAll());
        }
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(workOrderService.listPage(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkOrderResponse> getWorkOrder(@PathVariable Long id) {
        return ResponseEntity.ok(workOrderService.getById(id));
    }

    @PostMapping
    public ResponseEntity<WorkOrderResponse> createWorkOrder(
            @Valid @RequestBody CreateWorkOrderRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        WorkOrderResponse response = workOrderService.createWorkOrder(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<WorkOrderResponse> assignWorkOrder(
            @PathVariable Long id,
            @Valid @RequestBody AssignWorkOrderRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        WorkOrderResponse response = workOrderService.assignWorkOrder(id, request, userId);
        return ResponseEntity.ok(response);
    }
}
