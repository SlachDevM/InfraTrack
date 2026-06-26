package com.infratrack.user.dto;

import com.infratrack.department.Department;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserStatus;

public class UserManagementResponse {
    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private UserStatus status;
    private Long departmentId;
    private String departmentName;
    private Long createdAt;
    private Long updatedAt;

    public UserManagementResponse() {}

    public UserManagementResponse(
            Long id,
            String name,
            String email,
            UserRole role,
            UserStatus status,
            Long departmentId,
            String departmentName,
            Long createdAt,
            Long updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserManagementResponse from(User user, UserStatus status) {
        Department department = user.getDepartment();
        return new UserManagementResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                status,
                department != null ? department.getId() : null,
                department != null ? department.getName() : null,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
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

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
