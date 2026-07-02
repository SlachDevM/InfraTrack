package com.infratrack.organization.policy.visibility;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InspectionVisibilityPolicyServiceTest {

    @Test
    void getPolicy_shouldReturnDepartmentPolicy() {
        InspectionVisibilityPolicyService service = new InspectionVisibilityPolicyService();

        assertThat(service.getPolicy()).isInstanceOf(DepartmentInspectionVisibilityPolicy.class);
    }
}

