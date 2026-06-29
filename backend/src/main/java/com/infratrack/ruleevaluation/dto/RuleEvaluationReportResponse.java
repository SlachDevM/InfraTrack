package com.infratrack.ruleevaluation.dto;

import com.infratrack.ruleevaluation.RuleEvaluationReport;
import com.infratrack.ruleevaluation.RuleEvaluationStatus;

import java.util.Collections;
import java.util.List;

public class RuleEvaluationReportResponse {

    private Long id;
    private Long inspectionId;
    private Long evaluatedAt;
    private String engineVersion;
    private long evaluationDurationMs;
    private int resultCount;
    private int matchedCount;
    private Integer templateVersionSnapshot;
    private RuleEvaluationStatus evaluationStatus;
    private Long createdAt;
    private List<RuleEvaluationResultResponse> results = Collections.emptyList();

    public static RuleEvaluationReportResponse from(RuleEvaluationReport report) {
        return from(report, true);
    }

    public static RuleEvaluationReportResponse from(RuleEvaluationReport report, boolean includeResults) {
        RuleEvaluationReportResponse response = new RuleEvaluationReportResponse();
        response.id = report.getId();
        response.inspectionId = report.getInspection().getId();
        response.evaluatedAt = report.getEvaluatedAt();
        response.engineVersion = report.getEngineVersion();
        response.evaluationDurationMs = report.getEvaluationDurationMs();
        response.resultCount = report.getResultCount();
        response.matchedCount = report.getMatchedCount();
        response.templateVersionSnapshot = report.getTemplateVersionSnapshot();
        response.evaluationStatus = report.getEvaluationStatus();
        response.createdAt = report.getCreatedAt();
        if (includeResults) {
            response.results = report.getResults().stream()
                    .map(RuleEvaluationResultResponse::from)
                    .toList();
        }
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getInspectionId() {
        return inspectionId;
    }

    public Long getEvaluatedAt() {
        return evaluatedAt;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public long getEvaluationDurationMs() {
        return evaluationDurationMs;
    }

    public int getResultCount() {
        return resultCount;
    }

    public int getMatchedCount() {
        return matchedCount;
    }

    public Integer getTemplateVersionSnapshot() {
        return templateVersionSnapshot;
    }

    public RuleEvaluationStatus getEvaluationStatus() {
        return evaluationStatus;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public List<RuleEvaluationResultResponse> getResults() {
        return results;
    }
}
