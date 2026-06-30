package com.infratrack.operationsintelligence.dashboard;

import com.infratrack.exception.BusinessValidationException;

import java.util.Arrays;
import java.util.List;

public enum DashboardWidgetType {
    OVERVIEW,
    ATTENTION,
    TRENDS,
    RECENT_ACTIVITY,
    QUICK_NAVIGATION;

    public static final List<DashboardWidgetType> DEFAULT_ORDER = List.of(
            OVERVIEW,
            ATTENTION,
            TRENDS,
            RECENT_ACTIVITY,
            QUICK_NAVIGATION);

    public static DashboardWidgetType parse(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessValidationException("Widget type is required.");
        }
        try {
            return DashboardWidgetType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessValidationException(
                    "Unsupported widget type. Allowed values: "
                            + String.join(", ", Arrays.stream(values()).map(Enum::name).toList()));
        }
    }
}
