package com.infratrack.user.dto;

import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserStatus;
import com.infratrack.department.Department;

public class UserSummary {
    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private UserStatus status;
    private Long departmentId;

    public UserSummary() {
    }

    public UserSummary(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.status = user.getStatus();
        Department department = user.getDepartment();
        this.departmentId = department != null ? department.getId() : null;
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
}
