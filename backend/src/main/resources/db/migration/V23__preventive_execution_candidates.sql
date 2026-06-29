-- V2 Sprint B2: preventive maintenance execution candidates (review queue only; no workflow execution)

CREATE TABLE preventive_execution_candidates (
    id                                    BIGSERIAL PRIMARY KEY,
    preventive_plan_id                    BIGINT       NOT NULL,
    asset_id                              BIGINT       NOT NULL,
    candidate_status                      VARCHAR(50)  NOT NULL,
    trigger_type                          VARCHAR(50)  NOT NULL,
    eligibility_reason                    TEXT         NOT NULL,
    evaluated_at                          BIGINT       NOT NULL,
    next_eligible_at                      BIGINT,
    plan_code_snapshot                    VARCHAR(100) NOT NULL,
    plan_version_snapshot                 INTEGER      NOT NULL,
    plan_name_snapshot                    VARCHAR(255) NOT NULL,
    target_action_snapshot                VARCHAR(50)  NOT NULL,
    trigger_summary_title_snapshot        VARCHAR(255) NOT NULL,
    trigger_summary_description_snapshot  TEXT         NOT NULL,
    created_at                            BIGINT       NOT NULL,
    updated_at                            BIGINT       NOT NULL,
    CONSTRAINT fk_preventive_execution_candidates_plan
        FOREIGN KEY (preventive_plan_id) REFERENCES preventive_maintenance_plans (id),
    CONSTRAINT fk_preventive_execution_candidates_asset
        FOREIGN KEY (asset_id) REFERENCES assets (id)
);

CREATE INDEX idx_preventive_execution_candidates_plan_id
    ON preventive_execution_candidates (preventive_plan_id);
CREATE INDEX idx_preventive_execution_candidates_asset_id
    ON preventive_execution_candidates (asset_id);
CREATE INDEX idx_preventive_execution_candidates_status
    ON preventive_execution_candidates (candidate_status);
CREATE INDEX idx_preventive_execution_candidates_evaluated_at
    ON preventive_execution_candidates (evaluated_at);

CREATE UNIQUE INDEX ux_preventive_candidate_pending_plan
    ON preventive_execution_candidates (preventive_plan_id)
    WHERE candidate_status = 'PENDING';
