-- V2 Sprint B5: controlled preventive scheduler run history

CREATE TABLE preventive_scheduler_runs (
    id                                  BIGSERIAL PRIMARY KEY,
    started_at                          BIGINT       NOT NULL,
    finished_at                         BIGINT       NOT NULL,
    duration_ms                         BIGINT       NOT NULL,
    status                              VARCHAR(50)  NOT NULL,
    triggered_by                        VARCHAR(50)  NOT NULL,
    triggered_by_user_id                BIGINT,
    plans_evaluated_count               INTEGER      NOT NULL,
    candidates_created_count            INTEGER      NOT NULL,
    candidates_skipped_duplicate_count  INTEGER      NOT NULL,
    plans_not_eligible_count            INTEGER      NOT NULL,
    error_message                       TEXT,
    created_at                          BIGINT       NOT NULL
);

CREATE INDEX idx_preventive_scheduler_runs_started_at
    ON preventive_scheduler_runs (started_at);
CREATE INDEX idx_preventive_scheduler_runs_status
    ON preventive_scheduler_runs (status);
CREATE INDEX idx_preventive_scheduler_runs_triggered_by
    ON preventive_scheduler_runs (triggered_by);
