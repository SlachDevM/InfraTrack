package com.infratrack.inspectiontemplate;

import jakarta.persistence.*;

@Entity
@Table(
        name = "inspection_template_question_rules",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_inspection_template_question_rules_question_code",
                columnNames = {"question_id", "rule_code"}
        )
)
public class InspectionTemplateQuestionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private InspectionTemplateQuestion question;

    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 200)
    private String ruleName;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 50)
    private DecisionRuleConditionType conditionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DecisionRuleOperator operator;

    @Column(name = "comparison_value", length = 500)
    private String comparisonValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private DecisionRuleActionType actionType;

    @Column(name = "action_payload", columnDefinition = "TEXT")
    private String actionPayload;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private int priority = 100;

    @Column(name = "disabled_reason", length = 2000)
    private String disabledReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    protected InspectionTemplateQuestionRule() {
    }

    public InspectionTemplateQuestionRule(
            InspectionTemplateQuestion question,
            String ruleCode,
            String ruleName,
            String description,
            DecisionRuleConditionType conditionType,
            DecisionRuleOperator operator,
            String comparisonValue,
            DecisionRuleActionType actionType,
            String actionPayload) {
        this.question = question;
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.description = description;
        this.conditionType = conditionType;
        this.operator = operator;
        this.comparisonValue = comparisonValue;
        this.actionType = actionType;
        this.actionPayload = actionPayload;
        this.active = true;
        this.priority = 100;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InspectionTemplateQuestion getQuestion() {
        return question;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DecisionRuleConditionType getConditionType() {
        return conditionType;
    }

    public void setConditionType(DecisionRuleConditionType conditionType) {
        this.conditionType = conditionType;
    }

    public DecisionRuleOperator getOperator() {
        return operator;
    }

    public void setOperator(DecisionRuleOperator operator) {
        this.operator = operator;
    }

    public String getComparisonValue() {
        return comparisonValue;
    }

    public void setComparisonValue(String comparisonValue) {
        this.comparisonValue = comparisonValue;
    }

    public DecisionRuleActionType getActionType() {
        return actionType;
    }

    public void setActionType(DecisionRuleActionType actionType) {
        this.actionType = actionType;
    }

    public String getActionPayload() {
        return actionPayload;
    }

    public void setActionPayload(String actionPayload) {
        this.actionPayload = actionPayload;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getDisabledReason() {
        return disabledReason;
    }

    public void setDisabledReason(String disabledReason) {
        this.disabledReason = disabledReason;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void touchUpdatedAt() {
        this.updatedAt = System.currentTimeMillis();
    }
}
