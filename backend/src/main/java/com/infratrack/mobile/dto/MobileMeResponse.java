package com.infratrack.mobile.dto;

import com.infratrack.user.User;

public class MobileMeResponse {

    private Long id;
    private String name;
    private String email;
    private String role;
    private Long departmentId;
    private String departmentName;
    private boolean enabled;

    public static MobileMeResponse from(User user) {
        MobileMeResponse response = new MobileMeResponse();
        response.id = user.getId();
        response.name = user.getName();
        response.email = user.getEmail();
        response.role = user.getRole() != null ? user.getRole().name() : null;
        if (user.getDepartment() != null) {
            response.departmentId = user.getDepartment().getId();
            response.departmentName = user.getDepartment().getName();
        }
        response.enabled = Boolean.TRUE.equals(user.getEnabled());
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
