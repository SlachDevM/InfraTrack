package com.infratrack.mobile.dto;

import com.infratrack.operationaldecision.OperationalDecision;
import com.infratrack.operationaldecision.OperationalDecisionOutcome;

public class MobileDecisionSummaryResponse {

    private Long operationalDecisionId;
    private OperationalDecisionOutcome decisionType;

    public static MobileDecisionSummaryResponse from(OperationalDecision decision) {
        MobileDecisionSummaryResponse response = new MobileDecisionSummaryResponse();
        response.operationalDecisionId = decision.getId();
        response.decisionType = decision.getOutcome();
        return response;
    }

    public Long getOperationalDecisionId() {
        return operationalDecisionId;
    }

    public OperationalDecisionOutcome getDecisionType() {
        return decisionType;
    }
}
