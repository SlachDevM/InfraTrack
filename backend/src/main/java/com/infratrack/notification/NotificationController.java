package com.infratrack.notification;

import com.infratrack.config.PaginationSupport;
import com.infratrack.notification.dto.NotificationResponse;
import com.infratrack.notification.dto.NotificationSummaryResponse;
import com.infratrack.security.JwtAuthenticationToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<?> getUserNotifications(
            Authentication authentication,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Long userId = getUserId(authentication);
        if (PaginationSupport.isUnpagedRequest(page, size)) {
            List<NotificationSummaryResponse> responses = notificationService.getUserNotifications(userId).stream()
                    .map(NotificationSummaryResponse::from)
                    .toList();
            return ResponseEntity.ok(responses);
        }
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
    public ResponseEntity<?> getUnreadNotifications(
            Authentication authentication,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Long userId = getUserId(authentication);
        if (PaginationSupport.isUnpagedRequest(page, size)) {
            List<NotificationSummaryResponse> responses = notificationService.getUnreadNotifications(userId).stream()
                    .map(NotificationSummaryResponse::from)
                    .toList();
            return ResponseEntity.ok(responses);
        }
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
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable("id") Long id, Authentication authentication) {
        return ResponseEntity.ok(NotificationResponse.from(notificationService.markAsRead(id)));
    }

    @PutMapping("/read-all")
    public ResponseEntity<String> markAllAsRead(Authentication authentication) {
        Long userId = getUserId(authentication);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("All notifications marked as read");
    }

    private Long getUserId(Authentication authentication) {
        return ((JwtAuthenticationToken) authentication).getUserId();
    }
}
