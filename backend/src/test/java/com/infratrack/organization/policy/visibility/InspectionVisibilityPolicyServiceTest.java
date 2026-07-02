package com.infratrack.organization.policy.visibility;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InspectionVisibilityPolicyServiceTest {

    @Test
    void getPolicy_shouldReturnDepartmentPolicy() {
        InspectionVisibilityPolicyService service = new InspectionVisibilityPolicyService("DEPARTMENT");

        assertThat(service.getPolicy()).isInstanceOf(DepartmentInspectionVisibilityPolicy.class);
    }

    @Test
    void getPolicy_shouldReturnOrganizationPolicy_whenConfigured() {
        InspectionVisibilityPolicyService service = new InspectionVisibilityPolicyService("ORGANIZATION");

        assertThat(service.getPolicy()).isInstanceOf(OrganizationInspectionVisibilityPolicy.class);
    }

    @Test
    void getPolicy_shouldDefaultToDepartment_whenBlank() {
        InspectionVisibilityPolicyService service = new InspectionVisibilityPolicyService("  ");

        assertThat(service.getPolicy()).isInstanceOf(DepartmentInspectionVisibilityPolicy.class);
    }

    @Test
    void constructor_shouldFailFast_whenInvalidValueProvided() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> new InspectionVisibilityPolicyService("NOPE"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid inspection visibility policy");
    }
}

