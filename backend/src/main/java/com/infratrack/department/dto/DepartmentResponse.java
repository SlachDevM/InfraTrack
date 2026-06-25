package com.infratrack.department.dto;

import com.infratrack.department.Department;

public class DepartmentResponse {

    private Long id;
    private String name;
    private Long createdAt;
    private Long updatedAt;

    public DepartmentResponse() {
    }

    public DepartmentResponse(Long id, String name, Long createdAt, Long updatedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
