package com.infratrack.mobile.sync.dto;

import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionAnswer;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.inspection.PhysicalCondition;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Compact inspection record for mobile sync delta download (M5.4-BE).
 */
public class SyncInspectionDeltaResponse {

    private Long id;

    private InspectionStatus status;

    private InspectionPriority priority;

    private Long assignedToUserId;

    private String assignedToName;

    private SyncInspectionAssetDeltaResponse asset;

    private PhysicalCondition observedCondition;

    private String observations;

    private boolean issueIdentified;

    @Schema(description = "Expected completion date (yyyy-MM-dd)")
    private LocalDate expectedCompletionDate;

    private LocalDateTime completedAt;

    @Schema(description = "Server-side last update time (epoch millis)")
    private Long updatedAt;

    private List<SyncInspectionAnswerDeltaResponse> answers = new ArrayList<>();

    public static SyncInspectionDeltaResponse from(
            Inspection inspection,
            List<InspectionAnswer> answers,
            String assignedToName) {
        SyncInspectionDeltaResponse response = new SyncInspectionDeltaResponse();
        response.setId(inspection.getId());
        response.setStatus(inspection.getStatus());
        response.setPriority(inspection.getPriority());
        response.setAssignedToUserId(inspection.getAssignedToUserId());
        response.setAssignedToName(assignedToName);
        response.setAsset(toAsset(inspection));
        response.setObservedCondition(inspection.getObservedCondition());
        response.setObservations(inspection.getObservations());
        response.setIssueIdentified(inspection.isIssueIdentified());
        response.setExpectedCompletionDate(inspection.getExpectedCompletionDate());
        response.setCompletedAt(inspection.getCompletedAt());
        response.setUpdatedAt(inspection.getUpdatedAt());
        response.setAnswers(mapAnswers(answers));
        return response;
    }

    private static SyncInspectionAssetDeltaResponse toAsset(Inspection inspection) {
        SyncInspectionAssetDeltaResponse asset = new SyncInspectionAssetDeltaResponse();
        asset.setAssetId(inspection.getAsset().getId());
        asset.setAssetName(inspection.getAsset().getName());
        if (inspection.getAsset().getAssetCategory() != null) {
            asset.setAssetCategoryName(inspection.getAsset().getAssetCategory().getName());
        }
        return asset;
    }

    private static List<SyncInspectionAnswerDeltaResponse> mapAnswers(List<InspectionAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            return List.of();
        }
        List<SyncInspectionAnswerDeltaResponse> mapped = new ArrayList<>(answers.size());
        for (InspectionAnswer answer : answers) {
            SyncInspectionAnswerDeltaResponse item = new SyncInspectionAnswerDeltaResponse();
            item.setQuestionId(answer.getQuestion().getId());
            item.setBooleanValue(answer.getBooleanValue());
            item.setTextValue(answer.getTextValue());
            item.setNumberValue(answer.getNumberValue());
            item.setChoiceCodeValue(answer.getChoiceCodeValue());
            mapped.add(item);
        }
        return mapped;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InspectionStatus getStatus() {
        return status;
    }

    public void setStatus(InspectionStatus status) {
        this.status = status;
    }

    public InspectionPriority getPriority() {
        return priority;
    }

    public void setPriority(InspectionPriority priority) {
        this.priority = priority;
    }

    public Long getAssignedToUserId() {
        return assignedToUserId;
    }

    public void setAssignedToUserId(Long assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    public SyncInspectionAssetDeltaResponse getAsset() {
        return asset;
    }

    public void setAsset(SyncInspectionAssetDeltaResponse asset) {
        this.asset = asset;
    }

    public PhysicalCondition getObservedCondition() {
        return observedCondition;
    }

    public void setObservedCondition(PhysicalCondition observedCondition) {
        this.observedCondition = observedCondition;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public boolean isIssueIdentified() {
        return issueIdentified;
    }

    public void setIssueIdentified(boolean issueIdentified) {
        this.issueIdentified = issueIdentified;
    }

    public LocalDate getExpectedCompletionDate() {
        return expectedCompletionDate;
    }

    public void setExpectedCompletionDate(LocalDate expectedCompletionDate) {
        this.expectedCompletionDate = expectedCompletionDate;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<SyncInspectionAnswerDeltaResponse> getAnswers() {
        return answers;
    }

    public void setAnswers(List<SyncInspectionAnswerDeltaResponse> answers) {
        this.answers = answers != null ? answers : new ArrayList<>();
    }
}
