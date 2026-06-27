package com.infratrack.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailNormalizerTest {

    @Test
    void normalize_convertsMixedCaseAndTrims() {
        assertThat(EmailNormalizer.normalize("  John.Doe@Company.com  "))
                .isEqualTo("john.doe@company.com");
    }

    @Test
    void normalize_returnsNullForNullInput() {
        assertThat(EmailNormalizer.normalize(null)).isNull();
    }
}
