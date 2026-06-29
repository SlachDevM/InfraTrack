-- V2 Sprint B4: preventive execution audit reports (one report per execution candidate)

CREATE TABLE preventive_execution_reports (
    id                                        BIGSERIAL PRIMARY KEY,
    candidate_id                              BIGINT       NOT NULL,
    preventive_maintenance_plan_id_snapshot   BIGINT       NOT NULL,
    plan_code_snapshot                        VARCHAR(100) NOT NULL,
    plan_version_snapshot                     INTEGER      NOT NULL,
    plan_name_snapshot                        VARCHAR(255) NOT NULL,
    asset_id_snapshot                         BIGINT       NOT NULL,
    asset_name_snapshot                       VARCHAR(255) NOT NULL,
    target_action_snapshot                    VARCHAR(50)  NOT NULL,
    trigger_type_snapshot                     VARCHAR(50)  NOT NULL,
    trigger_summary_title_snapshot            VARCHAR(255) NOT NULL,
    trigger_summary_description_snapshot      TEXT         NOT NULL,
    decision_source                           VARCHAR(50)  NOT NULL,
    report_status                             VARCHAR(50)  NOT NULL,
    generated_at                              BIGINT       NOT NULL,
    review_started_at                         BIGINT,
    approved_at                               BIGINT,
    rejected_at                               BIGINT,
    dismissed_at                              BIGINT,
    inspection_created_at                     BIGINT,
    created_inspection_id                     BIGINT,
    decided_by_user_id                        BIGINT,
    decision_reason                           TEXT,
    created_at                                BIGINT       NOT NULL,
    updated_at                                BIGINT       NOT NULL,
    CONSTRAINT fk_preventive_execution_reports_candidate
        FOREIGN KEY (candidate_id) REFERENCES preventive_execution_candidates (id),
    CONSTRAINT ux_preventive_execution_reports_candidate
        UNIQUE (candidate_id)
);

CREATE INDEX idx_preventive_execution_reports_candidate_id
    ON preventive_execution_reports (candidate_id);
CREATE INDEX idx_preventive_execution_reports_status
    ON preventive_execution_reports (report_status);
CREATE INDEX idx_preventive_execution_reports_asset_id_snapshot
    ON preventive_execution_reports (asset_id_snapshot);
CREATE INDEX idx_preventive_execution_reports_plan_id_snapshot
    ON preventive_execution_reports (preventive_maintenance_plan_id_snapshot);
CREATE INDEX idx_preventive_execution_reports_decision_source
    ON preventive_execution_reports (decision_source);
