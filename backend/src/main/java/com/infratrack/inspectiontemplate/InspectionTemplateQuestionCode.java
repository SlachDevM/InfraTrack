package com.infratrack.inspectiontemplate;

import com.infratrack.exception.BusinessValidationException;

import java.util.regex.Pattern;

/**
 * Validates and derives stable business codes for checklist questions.
 */
public final class InspectionTemplateQuestionCode {

    static final int MAX_LENGTH = 100;
    static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");

    private InspectionTemplateQuestionCode() {
    }

    public static String normalize(String code) {
        if (code == null) {
            throw new BusinessValidationException("Question code is required");
        }
        String normalized = code.trim().toUpperCase();
        if (normalized.isBlank()) {
            throw new BusinessValidationException("Question code is required");
        }
        return normalized;
    }

    public static void validateFormat(String code) {
        if (code.length() > MAX_LENGTH) {
            throw new BusinessValidationException(
                    "Question code must be at most " + MAX_LENGTH + " characters");
        }
        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new BusinessValidationException(
                    "Question code must be uppercase, start with a letter, and contain only letters, digits, and underscores");
        }
    }

    public static String suggestFromQuestionText(String questionText) {
        if (questionText == null || questionText.isBlank()) {
            return "";
        }

        String normalized = questionText.trim()
                .replaceAll("[^a-zA-Z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "")
                .toUpperCase();

        if (normalized.isEmpty()) {
            return "QUESTION";
        }
        if (!Character.isLetter(normalized.charAt(0))) {
            normalized = "Q_" + normalized;
        }
        if (normalized.length() > MAX_LENGTH) {
            normalized = normalized.substring(0, MAX_LENGTH).replaceAll("_+$", "");
        }
        return normalized;
    }
}
