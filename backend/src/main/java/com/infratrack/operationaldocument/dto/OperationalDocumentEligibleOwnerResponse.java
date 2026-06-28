package com.infratrack.operationaldocument.dto;

import java.time.LocalDate;

public class OperationalDocumentEligibleOwnerResponse {

    private Long id;
    private String label;
    private String status;
    private LocalDate businessDate;
    private String contextSummary;

    public static OperationalDocumentEligibleOwnerResponse of(
            Long id,
            String label,
            String status,
            LocalDate businessDate,
            String contextSummary) {
        OperationalDocumentEligibleOwnerResponse response = new OperationalDocumentEligibleOwnerResponse();
        response.id = id;
        response.label = label;
        response.status = status;
        response.businessDate = businessDate;
        response.contextSummary = contextSummary;
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public String getContextSummary() {
        return contextSummary;
    }
}
