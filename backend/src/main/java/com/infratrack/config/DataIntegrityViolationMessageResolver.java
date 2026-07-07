package com.infratrack.config;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.Locale;
import java.util.Map;

/**
 * Maps known database unique-constraint identifiers to business-oriented conflict messages.
 * Used only when a race bypasses application-level duplicate checks.
 */
final class DataIntegrityViolationMessageResolver {

    static final String FALLBACK_MESSAGE = "Request conflicts with existing data";

    private static final Map<String, String> CONSTRAINT_MESSAGES = Map.ofEntries(
            Map.entry("uk_issues_inspection_id", "An issue has already been recorded for this inspection"),
            Map.entry("uk_issues_source_completion_review_id",
                    "A rework issue has already been created for this completion review"),
            Map.entry("uk_operational_decisions_issue_id",
                    "An operational decision has already been made for this issue"),
            Map.entry("uk_work_orders_operational_decision_id",
                    "A work order has already been created for this operational decision"),
            Map.entry("uk_maintenance_activities_work_order_id",
                    "Maintenance has already been completed for this work order"),
            Map.entry("uk_completion_reviews_maintenance_activity_id",
                    "A completion review has already been recorded for this maintenance activity"),
            Map.entry("uk_inspection_answers_inspection_question",
                    "Duplicate answer submitted for the same checklist question"),
            Map.entry("uk_users_email", "Email already exists"),
            Map.entry("uk_departments_name", "Department name already exists"),
            Map.entry("uk_asset_categories_name", "Asset category name already exists"),
            Map.entry("idx_assets_asset_code", "Asset code already exists"),
            Map.entry("uq_preventive_maintenance_plans_plan_code", "Plan code already exists"),
            Map.entry("ux_preventive_candidate_pending_plan",
                    "A pending execution candidate already exists for this plan"),
            Map.entry("idx_inspection_template_questions_template_id_code",
                    "Question code already exists for this template"),
            Map.entry("uk_inspection_template_question_choices_question_code",
                    "Choice code already exists for this question"),
            Map.entry("uk_inspection_template_question_rules_question_code",
                    "Rule code already exists for this question"),
            Map.entry("mobile_sync_operation_pkey", "Sync operation has already been processed")
    );

    private DataIntegrityViolationMessageResolver() {
    }

    static String resolveMessage(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            String message = cause.getMessage();
            if (message == null || message.isBlank()) {
                continue;
            }
            String normalized = message.toLowerCase(Locale.ROOT);
            for (Map.Entry<String, String> entry : CONSTRAINT_MESSAGES.entrySet()) {
                if (normalized.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return FALLBACK_MESSAGE;
    }
}
