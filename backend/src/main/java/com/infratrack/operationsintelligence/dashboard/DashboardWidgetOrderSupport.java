package com.infratrack.operationsintelligence.dashboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.validation.JsonPayloadSupport;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

final class DashboardWidgetOrderSupport {

    private DashboardWidgetOrderSupport() {
    }

    static String serializeDefaultOrder() {
        return serialize(DashboardWidgetType.DEFAULT_ORDER);
    }

    static String serialize(List<DashboardWidgetType> order) {
        try {
            return JsonPayloadSupport.objectMapper().writeValueAsString(order.stream().map(Enum::name).toList());
        } catch (JsonProcessingException ex) {
            throw new BusinessValidationException("Unable to persist widget order.");
        }
    }

    static List<DashboardWidgetType> parseAndNormalize(List<String> widgetOrder) {
        if (widgetOrder == null || widgetOrder.isEmpty()) {
            return DashboardWidgetType.DEFAULT_ORDER;
        }

        LinkedHashSet<DashboardWidgetType> normalized = new LinkedHashSet<>();
        for (String value : widgetOrder) {
            DashboardWidgetType type = DashboardWidgetType.parse(value);
            normalized.add(type);
        }

        for (DashboardWidgetType type : DashboardWidgetType.DEFAULT_ORDER) {
            normalized.add(type);
        }

        return new ArrayList<>(normalized);
    }

    static List<DashboardWidgetType> parseStored(String widgetOrderJson) {
        try {
            List<String> values = JsonPayloadSupport.objectMapper().readValue(widgetOrderJson, new TypeReference<>() {
            });
            return parseAndNormalize(values);
        } catch (JsonProcessingException ex) {
            return DashboardWidgetType.DEFAULT_ORDER;
        }
    }
}
