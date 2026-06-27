package com.infratrack.notification.dto;

import com.infratrack.notification.Notification;

public class NotificationSummaryResponse {

    private Long id;
    private String title;
    private String message;
    private Boolean isRead;
    private String targetRoute;
    private Long createdAt;

    public static NotificationSummaryResponse from(Notification notification) {
        NotificationSummaryResponse response = new NotificationSummaryResponse();
        response.id = notification.getId();
        response.title = notification.getTitle();
        response.message = notification.getMessage();
        response.isRead = notification.getIsRead();
        response.targetRoute = notification.getTargetRoute();
        response.createdAt = notification.getCreatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public String getTargetRoute() {
        return targetRoute;
    }

    public Long getCreatedAt() {
        return createdAt;
    }
}
