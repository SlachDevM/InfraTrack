package com.infratrack.user.dto;

import com.infratrack.department.Department;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;

public class UserProfileResponse {
    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private Long departmentId;
    private String departmentName;

    public UserProfileResponse() {
    }

    public UserProfileResponse(Long id, String name, String email, UserRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public UserProfileResponse(
            Long id,
            String name,
            String email,
            UserRole role,
            Long departmentId,
            String departmentName) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }

    public static UserProfileResponse from(User user) {
        Department department = user.getDepartment();
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                department != null ? department.getId() : null,
                department != null ? department.getName() : null
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
