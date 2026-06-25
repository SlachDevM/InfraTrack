package com.infratrack.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRoleTest {

    @Test
    void roleIdentityHelpers_matchExpectedRole() {
        assertThat(UserRole.ADMINISTRATOR.isAdministrator()).isTrue();
        assertThat(UserRole.ADMINISTRATOR.isManager()).isFalse();

        assertThat(UserRole.MANAGER.isManager()).isTrue();
        assertThat(UserRole.MANAGER.isAdministrator()).isFalse();

        assertThat(UserRole.OPERATIONAL_COORDINATOR.isOperationalCoordinator()).isTrue();
        assertThat(UserRole.OPERATIONAL_COORDINATOR.isManager()).isFalse();

        assertThat(UserRole.FIELD_EMPLOYEE.isFieldEmployee()).isTrue();
        assertThat(UserRole.FIELD_EMPLOYEE.isContractor()).isFalse();

        assertThat(UserRole.CONTRACTOR.isContractor()).isTrue();
        assertThat(UserRole.CONTRACTOR.isFieldEmployee()).isFalse();
    }
}
