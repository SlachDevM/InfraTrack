package com.infratrack.operationsintelligence;

import com.infratrack.exception.BusinessValidationException;

public enum TrendBucket {
    DAY,
    WEEK,
    MONTH;

    public static TrendBucket parse(String value) {
        if (value == null || value.isBlank()) {
            return DAY;
        }
        try {
            return TrendBucket.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessValidationException(
                    "Unsupported trend bucket. Allowed values: DAY, WEEK, MONTH.");
        }
    }
}
