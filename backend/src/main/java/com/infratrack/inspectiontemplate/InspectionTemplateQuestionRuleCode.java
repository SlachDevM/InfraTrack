package com.infratrack.inspectiontemplate;

import com.infratrack.exception.BusinessValidationException;

import java.util.regex.Pattern;

/**
 * Validates stable business codes for decision rules on checklist questions.
 */
public final class InspectionTemplateQuestionRuleCode {

    static final int MAX_LENGTH = 100;
    static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");

    private InspectionTemplateQuestionRuleCode() {
    }

    public static String normalize(String code) {
        if (code == null) {
            throw new BusinessValidationException("Rule code is required");
        }
        String normalized = code.trim().toUpperCase();
        if (normalized.isBlank()) {
            throw new BusinessValidationException("Rule code is required");
        }
        return normalized;
    }

    public static void validateFormat(String code) {
        if (code.length() > MAX_LENGTH) {
            throw new BusinessValidationException(
                    "Rule code must be at most " + MAX_LENGTH + " characters");
        }
        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new BusinessValidationException(
                    "Rule code must be uppercase, start with a letter, and contain only letters, digits, and underscores");
        }
    }
}
