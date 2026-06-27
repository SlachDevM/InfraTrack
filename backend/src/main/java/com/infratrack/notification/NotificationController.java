package com.infratrack.notification;

import com.infratrack.config.PaginationSupport;
import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.notification.dto.NotificationResponse;
import com.infratrack.notification.dto.NotificationSummaryResponse;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "In-app notifications for the authenticated user (UC-013)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "List notifications", description = "Returns paginated notifications for the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Paginated notification summaries")
    public ResponseEntity<Page<NotificationSummaryResponse>> getUserNotifications(
            Authentication authentication,
            @Parameter(description = "Zero-based page index") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (max 100)") @RequestParam(required = false) Integer size) {
        Long userId = getUserId(authentication);
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NotificationSummaryResponse> responses = notificationService
                .getUserNotifications(userId, pageable)
                .map(NotificationSummaryResponse::from);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/unread")
    @Operation(summary = "List unread notifications")
    @ApiResponse(responseCode = "200", description = "Paginated unread notification summaries")
    public ResponseEntity<Page<NotificationSummaryResponse>> getUnreadNotifications(
            Authentication authentication,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Long userId = getUserId(authentication);
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NotificationSummaryResponse> responses = notificationService
                .getUnreadNotifications(userId, pageable)
                .map(NotificationSummaryResponse::from);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    @ApiResponse(responseCode = "200", description = "Unread count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    @ApiResponse(responseCode = "200", description = "Updated notification")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable("id") Long id, Authentication authentication) {
        return ResponseEntity.ok(NotificationResponse.from(notificationService.markAsRead(id)));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    @ApiResponse(responseCode = "200", description = "Confirmation message")
    public ResponseEntity<String> markAllAsRead(Authentication authentication) {
        Long userId = getUserId(authentication);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("All notifications marked as read");
    }

    private Long getUserId(Authentication authentication) {
        return ((JwtAuthenticationToken) authentication).getUserId();
    }
}
