-- V2 Sprint A3.3: persisted rule evaluation reports

CREATE TABLE rule_evaluation_reports (
    id BIGSERIAL PRIMARY KEY,
    inspection_id BIGINT NOT NULL,
    evaluated_at BIGINT NOT NULL,
    engine_version VARCHAR(50) NOT NULL,
    evaluation_duration_ms BIGINT NOT NULL,
    result_count INTEGER NOT NULL,
    matched_count INTEGER NOT NULL,
    created_at BIGINT NOT NULL,
    CONSTRAINT fk_rule_evaluation_reports_inspection
        FOREIGN KEY (inspection_id) REFERENCES inspections (id)
);

CREATE INDEX idx_rule_evaluation_reports_inspection_id
    ON rule_evaluation_reports (inspection_id);

CREATE INDEX idx_rule_evaluation_reports_inspection_evaluated_at
    ON rule_evaluation_reports (inspection_id, evaluated_at DESC);

CREATE TABLE rule_evaluation_results (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL,
    rule_id_snapshot BIGINT NOT NULL,
    rule_code_snapshot VARCHAR(100) NOT NULL,
    rule_name_snapshot VARCHAR(200) NOT NULL,
    condition_type_snapshot VARCHAR(50) NOT NULL,
    operator_snapshot VARCHAR(50) NOT NULL,
    comparison_value_snapshot VARCHAR(500),
    actual_value_snapshot VARCHAR(500),
    matched BOOLEAN NOT NULL,
    action_type_snapshot VARCHAR(50) NOT NULL,
    action_payload_snapshot TEXT,
    priority_snapshot INTEGER NOT NULL,
    evaluated_at BIGINT NOT NULL,
    evaluation_duration_ms BIGINT NOT NULL,
    CONSTRAINT fk_rule_evaluation_results_report
        FOREIGN KEY (report_id) REFERENCES rule_evaluation_reports (id)
);

CREATE INDEX idx_rule_evaluation_results_report_id
    ON rule_evaluation_results (report_id);

CREATE INDEX idx_rule_evaluation_results_report_matched
    ON rule_evaluation_results (report_id, matched);

CREATE INDEX idx_rule_evaluation_results_rule_code_snapshot
    ON rule_evaluation_results (rule_code_snapshot);
