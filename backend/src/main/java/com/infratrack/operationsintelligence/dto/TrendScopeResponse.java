package com.infratrack.operationsintelligence.dto;

public class TrendScopeResponse {

    private String type;
    private Long departmentId;

    public static TrendScopeResponse global() {
        TrendScopeResponse scope = new TrendScopeResponse();
        scope.setType("GLOBAL");
        scope.setDepartmentId(null);
        return scope;
    }

    public static TrendScopeResponse forDepartment(Long departmentId) {
        TrendScopeResponse scope = new TrendScopeResponse();
        scope.setType("DEPARTMENT");
        scope.setDepartmentId(departmentId);
        return scope;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}
