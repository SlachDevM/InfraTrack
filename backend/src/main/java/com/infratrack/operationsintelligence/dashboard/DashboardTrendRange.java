package com.infratrack.operationsintelligence.dashboard;

import com.infratrack.exception.BusinessValidationException;

import java.util.Arrays;

public enum DashboardTrendRange {
    LAST_7_DAYS(7),
    LAST_30_DAYS(30),
    LAST_90_DAYS(90);

    private final int days;

    DashboardTrendRange(int days) {
        this.days = days;
    }

    public int days() {
        return days;
    }

    public static DashboardTrendRange parse(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessValidationException("Trend range is required.");
        }
        try {
            return DashboardTrendRange.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessValidationException(
                    "Unsupported trend range. Allowed values: "
                            + String.join(", ", Arrays.stream(values()).map(Enum::name).toList()));
        }
    }
}
