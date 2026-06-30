package com.infratrack.mobile;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.mobile.dto.MobileDashboardResponse;
import com.infratrack.mobile.dto.MobileInspectionBundleResponse;
import com.infratrack.mobile.dto.MobileInspectionSummaryResponse;
import com.infratrack.mobile.dto.MobileMeResponse;
import com.infratrack.mobile.dto.MobileWorkOrderBundleResponse;
import com.infratrack.mobile.dto.MobileWorkOrderSummaryResponse;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mobile")
@Tag(name = "Mobile API", description = "Compact read/bundle endpoints for the future Android field client (V2.2.0 Sprint M1)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class MobileController {

    private final MobileService mobileService;

    public MobileController(MobileService mobileService) {
        this.mobileService = mobileService;
    }

    @GetMapping("/me")
    @Operation(summary = "Authenticated user summary for mobile startup")
    @ApiResponse(responseCode = "200", description = "Mobile identity summary")
    public ResponseEntity<MobileMeResponse> getMe(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(mobileService.getMe(userId));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Compact personal dashboard summary for mobile")
    @ApiResponse(responseCode = "200", description = "Personal assignment counts")
    public ResponseEntity<MobileDashboardResponse> getDashboard(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(mobileService.getDashboard(userId));
    }

    @GetMapping("/my-inspections")
    @Operation(summary = "Assigned inspections summary list for mobile")
    @ApiResponse(responseCode = "200", description = "Inspection summaries scoped to the authenticated user")
    public ResponseEntity<List<MobileInspectionSummaryResponse>> getMyInspections(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(mobileService.getMyInspections(userId));
    }

    @GetMapping("/inspections/{inspectionId}/bundle")
    @Operation(summary = "Inspection screen bundle with asset, template, questions and answers")
    @ApiResponse(responseCode = "200", description = "Inspection bundle for one mobile screen")
    public ResponseEntity<MobileInspectionBundleResponse> getInspectionBundle(
            Authentication authentication,
            @PathVariable Long inspectionId) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(mobileService.getInspectionBundle(userId, inspectionId));
    }

    @GetMapping("/my-work-orders")
    @Operation(summary = "Assigned work orders summary list for mobile")
    @ApiResponse(responseCode = "200", description = "Work order summaries scoped to the authenticated user")
    public ResponseEntity<List<MobileWorkOrderSummaryResponse>> getMyWorkOrders(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(mobileService.getMyWorkOrders(userId));
    }

    @GetMapping("/work-orders/{workOrderId}/bundle")
    @Operation(summary = "Work order screen bundle with asset, issue/decision and maintenance activity")
    @ApiResponse(responseCode = "200", description = "Work order bundle for one mobile screen")
    public ResponseEntity<MobileWorkOrderBundleResponse> getWorkOrderBundle(
            Authentication authentication,
            @PathVariable Long workOrderId) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(mobileService.getWorkOrderBundle(userId, workOrderId));
    }
}
