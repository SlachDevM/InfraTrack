package com.infratrack.workorder;

import com.infratrack.config.PaginationSupport;
import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.security.JwtAuthenticationToken;
import com.infratrack.workorder.dto.AssignWorkOrderRequest;
import com.infratrack.workorder.dto.CreateWorkOrderRequest;
import com.infratrack.workorder.dto.WorkOrderResponse;
import com.infratrack.workorder.dto.WorkOrderSummaryResponse;
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
@RequestMapping("/api/work-orders")
@Tag(name = "Work Orders", description = "Work order creation and assignment (UC-007, UC-008)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @GetMapping
    @Operation(summary = "List work orders", description = "Returns paginated work order summaries ordered by creation time.")
    @ApiResponse(responseCode = "200", description = "Paginated work order summaries")
    public ResponseEntity<Page<WorkOrderSummaryResponse>> listWorkOrders(
            @Parameter(description = "Zero-based page index") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (max 100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "When true, returns only CREATED work orders eligible for assignment (UC-008)")
            @RequestParam(required = false) Boolean eligibleForAssignment,
            Authentication authentication) {
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        if (Boolean.TRUE.equals(eligibleForAssignment)) {
            Long userId = ((JwtAuthenticationToken) authentication).getUserId();
            return ResponseEntity.ok(workOrderService.listEligibleForAssignmentPage(userId, pageable));
        }
        return ResponseEntity.ok(workOrderService.listPage(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get work order by ID")
    @ApiResponse(responseCode = "200", description = "Work order details")
    public ResponseEntity<WorkOrderResponse> getWorkOrder(@PathVariable Long id) {
        return ResponseEntity.ok(workOrderService.getById(id));
    }

    @PostMapping
    @Operation(
            summary = "Create work order",
            description = "Creates a work order from an operational decision authorising physical work (UC-007).")
    @ApiResponse(responseCode = "201", description = "Work order created")
    public ResponseEntity<WorkOrderResponse> createWorkOrder(
            @Valid @RequestBody CreateWorkOrderRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        WorkOrderResponse response = workOrderService.createWorkOrder(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/assign")
    @Operation(
            summary = "Assign work order",
            description = "Assigns a work order to a field employee or contractor (UC-008).")
    @ApiResponse(responseCode = "200", description = "Work order assigned")
    public ResponseEntity<WorkOrderResponse> assignWorkOrder(
            @PathVariable Long id,
            @Valid @RequestBody AssignWorkOrderRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        WorkOrderResponse response = workOrderService.assignWorkOrder(id, request, userId);
        return ResponseEntity.ok(response);
    }
}
