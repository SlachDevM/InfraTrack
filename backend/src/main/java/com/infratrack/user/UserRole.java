package com.infratrack.user;

public enum UserRole {
    ADMINISTRATOR,
    MANAGER,
    OPERATIONAL_COORDINATOR,
    FIELD_EMPLOYEE,
    CONTRACTOR;

    public boolean isAdministrator() {
        return this == ADMINISTRATOR;
    }

    public boolean isManager() {
        return this == MANAGER;
    }

    public boolean isOperationalCoordinator() {
        return this == OPERATIONAL_COORDINATOR;
    }

    public boolean isFieldEmployee() {
        return this == FIELD_EMPLOYEE;
    }

    public boolean isContractor() {
        return this == CONTRACTOR;
    }
}
