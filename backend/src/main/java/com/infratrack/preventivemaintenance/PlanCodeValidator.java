package com.infratrack.preventivemaintenance;

import com.infratrack.exception.BusinessValidationException;

import java.util.regex.Pattern;

final class PlanCodeValidator {

    private static final Pattern PLAN_CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$");

    private PlanCodeValidator() {
    }

    static String validateAndNormalize(String planCode) {
        if (planCode == null || planCode.isBlank()) {
            throw new BusinessValidationException("Plan code is required");
        }
        String normalized = planCode.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        if (!PLAN_CODE_PATTERN.matcher(normalized).matches()) {
            throw new BusinessValidationException(
                    "Plan code must be uppercase snake_case (for example PUMP_MONTHLY)");
        }
        return normalized;
    }
}
