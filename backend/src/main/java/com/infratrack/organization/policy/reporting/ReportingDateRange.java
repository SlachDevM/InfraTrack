package com.infratrack.organization.policy.reporting;

/**
 * Organizational default reporting period (BDR-004).
 *
 * <p>Export endpoints still accept explicit {@code from} and {@code to} query parameters.
 * When both are omitted, exports remain unfiltered until user or policy-driven defaults are applied.
 */
public enum ReportingDateRange {
    LAST_7_DAYS(7),
    LAST_30_DAYS(30),
    LAST_90_DAYS(90);

    private final int days;

    ReportingDateRange(int days) {
        this.days = days;
    }

    public int days() {
        return days;
    }
}
