-- V2 Sprint A3.4: suggested actions from rule evaluation reports

ALTER TABLE rule_evaluation_reports
    ADD COLUMN template_version_snapshot INTEGER,
    ADD COLUMN evaluation_status VARCHAR(50) NOT NULL DEFAULT 'SUCCESS';

CREATE TABLE suggested_actions (
    id BIGSERIAL PRIMARY KEY,
    inspection_id BIGINT NOT NULL,
    report_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    title VARCHAR(300) NOT NULL,
    message TEXT,
    severity VARCHAR(50),
    suggested_payload TEXT,
    matched_rule_count INTEGER NOT NULL,
    source_rule_codes VARCHAR(500) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT fk_suggested_actions_inspection
        FOREIGN KEY (inspection_id) REFERENCES inspections (id),
    CONSTRAINT fk_suggested_actions_report
        FOREIGN KEY (report_id) REFERENCES rule_evaluation_reports (id)
);

CREATE INDEX idx_suggested_actions_inspection_id
    ON suggested_actions (inspection_id);

CREATE INDEX idx_suggested_actions_report_id
    ON suggested_actions (report_id);

CREATE INDEX idx_suggested_actions_status
    ON suggested_actions (status);

CREATE INDEX idx_suggested_actions_action_type
    ON suggested_actions (action_type);
