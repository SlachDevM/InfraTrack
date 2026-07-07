package com.infratrack.config;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class DataIntegrityViolationMessageResolverTest {

    @Test
    void resolveMessage_mapsKnownUniqueConstraint() {
        DataIntegrityViolationException exception = constraintViolation(
                "ERROR: duplicate key value violates unique constraint \"uk_issues_inspection_id\"");

        assertThat(DataIntegrityViolationMessageResolver.resolveMessage(exception))
                .isEqualTo("An issue has already been recorded for this inspection");
    }

    @Test
    void resolveMessage_walksCauseChain() {
        SQLException sqlException = new SQLException(
                "duplicate key value violates unique constraint \"uk_operational_decisions_issue_id\"");
        DataIntegrityViolationException exception = new DataIntegrityViolationException("persist failed", sqlException);

        assertThat(DataIntegrityViolationMessageResolver.resolveMessage(exception))
                .isEqualTo("An operational decision has already been made for this issue");
    }

    @Test
    void resolveMessage_returnsFallbackForUnknownConstraint() {
        DataIntegrityViolationException exception = constraintViolation(
                "ERROR: duplicate key value violates unique constraint \"uk_unknown_constraint\"");

        assertThat(DataIntegrityViolationMessageResolver.resolveMessage(exception))
                .isEqualTo(DataIntegrityViolationMessageResolver.FALLBACK_MESSAGE);
    }

    private static DataIntegrityViolationException constraintViolation(String sqlMessage) {
        return new DataIntegrityViolationException("constraint violation", new SQLException(sqlMessage));
    }
}
