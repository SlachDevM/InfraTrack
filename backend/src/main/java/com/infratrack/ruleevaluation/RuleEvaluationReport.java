package com.infratrack.ruleevaluation;

import com.infratrack.inspection.Inspection;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rule_evaluation_reports")
public class RuleEvaluationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_id", nullable = false)
    private Inspection inspection;

    @Column(name = "evaluated_at", nullable = false)
    private Long evaluatedAt;

    @Column(name = "engine_version", nullable = false, length = 50)
    private String engineVersion;

    @Column(name = "evaluation_duration_ms", nullable = false)
    private long evaluationDurationMs;

    @Column(name = "result_count", nullable = false)
    private int resultCount;

    @Column(name = "matched_count", nullable = false)
    private int matchedCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RuleEvaluationResult> results = new ArrayList<>();

    protected RuleEvaluationReport() {
    }

    public RuleEvaluationReport(
            Inspection inspection,
            Long evaluatedAt,
            String engineVersion,
            long evaluationDurationMs,
            int resultCount,
            int matchedCount) {
        this.inspection = inspection;
        this.evaluatedAt = evaluatedAt;
        this.engineVersion = engineVersion;
        this.evaluationDurationMs = evaluationDurationMs;
        this.resultCount = resultCount;
        this.matchedCount = matchedCount;
        this.createdAt = System.currentTimeMillis();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Inspection getInspection() {
        return inspection;
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

    public Long getCreatedAt() {
        return createdAt;
    }

    public List<RuleEvaluationResult> getResults() {
        return results;
    }

    public void addResult(RuleEvaluationResult result) {
        results.add(result);
        result.setReport(this);
    }
}
