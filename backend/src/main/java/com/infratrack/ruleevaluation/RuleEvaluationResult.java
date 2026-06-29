package com.infratrack.ruleevaluation;

import com.infratrack.inspectiontemplate.DecisionRuleActionType;
import com.infratrack.inspectiontemplate.DecisionRuleConditionType;
import com.infratrack.inspectiontemplate.DecisionRuleOperator;
import jakarta.persistence.*;

@Entity
@Table(name = "rule_evaluation_results")
public class RuleEvaluationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private RuleEvaluationReport report;

    @Column(name = "rule_id_snapshot", nullable = false)
    private Long ruleIdSnapshot;

    @Column(name = "rule_code_snapshot", nullable = false, length = 100)
    private String ruleCodeSnapshot;

    @Column(name = "rule_name_snapshot", nullable = false, length = 200)
    private String ruleNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type_snapshot", nullable = false, length = 50)
    private DecisionRuleConditionType conditionTypeSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator_snapshot", nullable = false, length = 50)
    private DecisionRuleOperator operatorSnapshot;

    @Column(name = "comparison_value_snapshot", length = 500)
    private String comparisonValueSnapshot;

    @Column(name = "actual_value_snapshot", length = 500)
    private String actualValueSnapshot;

    @Column(nullable = false)
    private boolean matched;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type_snapshot", nullable = false, length = 50)
    private DecisionRuleActionType actionTypeSnapshot;

    @Column(name = "action_payload_snapshot", columnDefinition = "TEXT")
    private String actionPayloadSnapshot;

    @Column(name = "priority_snapshot", nullable = false)
    private int prioritySnapshot;

    @Column(name = "evaluated_at", nullable = false)
    private Long evaluatedAt;

    @Column(name = "evaluation_duration_ms", nullable = false)
    private long evaluationDurationMs;

    protected RuleEvaluationResult() {
    }

    public RuleEvaluationResult(
            Long ruleIdSnapshot,
            String ruleCodeSnapshot,
            String ruleNameSnapshot,
            DecisionRuleConditionType conditionTypeSnapshot,
            DecisionRuleOperator operatorSnapshot,
            String comparisonValueSnapshot,
            String actualValueSnapshot,
            boolean matched,
            DecisionRuleActionType actionTypeSnapshot,
            String actionPayloadSnapshot,
            int prioritySnapshot,
            Long evaluatedAt,
            long evaluationDurationMs) {
        this.ruleIdSnapshot = ruleIdSnapshot;
        this.ruleCodeSnapshot = ruleCodeSnapshot;
        this.ruleNameSnapshot = ruleNameSnapshot;
        this.conditionTypeSnapshot = conditionTypeSnapshot;
        this.operatorSnapshot = operatorSnapshot;
        this.comparisonValueSnapshot = comparisonValueSnapshot;
        this.actualValueSnapshot = actualValueSnapshot;
        this.matched = matched;
        this.actionTypeSnapshot = actionTypeSnapshot;
        this.actionPayloadSnapshot = actionPayloadSnapshot;
        this.prioritySnapshot = prioritySnapshot;
        this.evaluatedAt = evaluatedAt;
        this.evaluationDurationMs = evaluationDurationMs;
    }

    public void setReport(RuleEvaluationReport report) {
        this.report = report;
    }

    public Long getId() {
        return id;
    }

    public Long getRuleIdSnapshot() {
        return ruleIdSnapshot;
    }

    public String getRuleCodeSnapshot() {
        return ruleCodeSnapshot;
    }

    public String getRuleNameSnapshot() {
        return ruleNameSnapshot;
    }

    public DecisionRuleConditionType getConditionTypeSnapshot() {
        return conditionTypeSnapshot;
    }

    public DecisionRuleOperator getOperatorSnapshot() {
        return operatorSnapshot;
    }

    public String getComparisonValueSnapshot() {
        return comparisonValueSnapshot;
    }

    public String getActualValueSnapshot() {
        return actualValueSnapshot;
    }

    public boolean isMatched() {
        return matched;
    }

    public DecisionRuleActionType getActionTypeSnapshot() {
        return actionTypeSnapshot;
    }

    public String getActionPayloadSnapshot() {
        return actionPayloadSnapshot;
    }

    public int getPrioritySnapshot() {
        return prioritySnapshot;
    }

    public Long getEvaluatedAt() {
        return evaluatedAt;
    }

    public long getEvaluationDurationMs() {
        return evaluationDurationMs;
    }
}
