package com.infratrack.operationsintelligence.dto;

public class OperationsKpiResponse {

    private AssetKpiResponse assets;
    private InspectionKpiResponse inspections;
    private IssueKpiResponse issues;
    private WorkOrderKpiResponse workOrders;
    private PreventiveKpiResponse preventive;
    private DecisionEngineKpiResponse decisionEngine;

    public AssetKpiResponse getAssets() {
        return assets;
    }

    public void setAssets(AssetKpiResponse assets) {
        this.assets = assets;
    }

    public InspectionKpiResponse getInspections() {
        return inspections;
    }

    public void setInspections(InspectionKpiResponse inspections) {
        this.inspections = inspections;
    }

    public IssueKpiResponse getIssues() {
        return issues;
    }

    public void setIssues(IssueKpiResponse issues) {
        this.issues = issues;
    }

    public WorkOrderKpiResponse getWorkOrders() {
        return workOrders;
    }

    public void setWorkOrders(WorkOrderKpiResponse workOrders) {
        this.workOrders = workOrders;
    }

    public PreventiveKpiResponse getPreventive() {
        return preventive;
    }

    public void setPreventive(PreventiveKpiResponse preventive) {
        this.preventive = preventive;
    }

    public DecisionEngineKpiResponse getDecisionEngine() {
        return decisionEngine;
    }

    public void setDecisionEngine(DecisionEngineKpiResponse decisionEngine) {
        this.decisionEngine = decisionEngine;
    }
}
