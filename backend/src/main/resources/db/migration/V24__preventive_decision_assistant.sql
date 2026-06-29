-- V2 Sprint B3: preventive decision assistant — candidate review metadata and inspection traceability

ALTER TABLE preventive_execution_candidates
    ADD COLUMN decided_by_user_id BIGINT,
    ADD COLUMN decided_at BIGINT,
    ADD COLUMN rejection_reason TEXT,
    ADD COLUMN dismiss_comment TEXT,
    ADD COLUMN created_inspection_id BIGINT,
    ADD COLUMN decision_notes TEXT;

ALTER TABLE inspections
    ADD COLUMN preventive_execution_candidate_id BIGINT,
    ADD CONSTRAINT fk_inspections_preventive_execution_candidate
        FOREIGN KEY (preventive_execution_candidate_id) REFERENCES preventive_execution_candidates (id);

CREATE INDEX idx_inspections_preventive_execution_candidate_id
    ON inspections (preventive_execution_candidate_id);
