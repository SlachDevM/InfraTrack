package com.infratrack.user.dto;

public class UpdateUserRequest {
    private String name;
    private String email;
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
