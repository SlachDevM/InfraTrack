package com.infratrack.auth.dto;

import com.infratrack.user.UserRole;

public class LoginResponse {
    private Long userId;
    private String email;
    private String name;
    private UserRole role;
    private String token;
    private Long departmentId;
    private String departmentName;

    public LoginResponse() {}

    public LoginResponse(Long userId, String email, String name, UserRole role, String token) {
        this(userId, email, name, role, token, null, null);
    }

    public LoginResponse(
            Long userId,
            String email,
            String name,
            UserRole role,
            String token,
            Long departmentId,
            String departmentName) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.token = token;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
}
