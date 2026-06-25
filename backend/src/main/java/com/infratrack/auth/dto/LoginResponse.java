package com.infratrack.auth.dto;

import com.infratrack.user.UserRole;

public class LoginResponse {
    private Long userId;
    private String email;
    private String name;
    private UserRole role;
    private String token;

    public LoginResponse() {}

    public LoginResponse(Long userId, String email, String name, UserRole role, String token) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.token = token;
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
}
