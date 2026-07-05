package com.infratrack.mobile.sync;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SyncTokenTest {

    private static final Instant ISSUED_AT = Instant.parse("2026-07-05T08:30:00Z");

    @Test
    void issue_containsProtocolVersionAndIssuedAt() {
        SyncToken token = SyncToken.issue(ISSUED_AT);

        assertThat(token.getVersion()).isEqualTo(SyncProtocolVersion.CURRENT);
        assertThat(token.getIssuedAt()).isEqualTo(ISSUED_AT);
        assertThat(token.getToken()).isNotBlank();
    }

    @Test
    void issue_generatesUniqueTokens() {
        Set<String> opaqueValues = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            opaqueValues.add(SyncToken.issue(ISSUED_AT).toOpaqueValue());
        }
        assertThat(opaqueValues).hasSize(100);
    }

    @Test
    void opaqueValue_roundTripsWithoutExposingBusinessData() {
        SyncToken original = SyncToken.issue(ISSUED_AT);
        String opaque = original.toOpaqueValue();

        assertThat(opaque).doesNotContain("INSPECTION");
        assertThat(opaque).doesNotContain("WORK_ORDER");

        SyncToken decoded = SyncToken.fromOpaqueValue(opaque);
        assertThat(decoded.getVersion()).isEqualTo(original.getVersion());
        assertThat(decoded.getIssuedAt()).isEqualTo(original.getIssuedAt());
        assertThat(decoded.getToken()).isEqualTo(original.getToken());
    }

    @Test
    void fromOpaqueValue_rejectsBlankToken() {
        assertThatThrownBy(() -> SyncToken.fromOpaqueValue(" "))
                .isInstanceOf(com.infratrack.exception.BusinessValidationException.class)
                .hasMessage("Sync token is required.");
    }
}
