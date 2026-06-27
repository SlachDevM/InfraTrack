package com.infratrack.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class UpdateUserRequest {
    @Size(max = 255)
    private String name;

    @Email
    @Size(max = 255)
    private String email;

    @Positive
    private Long departmentId;

    private Boolean clearDepartment;

    public UpdateUserRequest() {}

    public UpdateUserRequest(String name, String email) {
        this.name = name;
        this.email = email;
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

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Boolean getClearDepartment() {
        return clearDepartment;
    }

    public void setClearDepartment(Boolean clearDepartment) {
        this.clearDepartment = clearDepartment;
    }
}
