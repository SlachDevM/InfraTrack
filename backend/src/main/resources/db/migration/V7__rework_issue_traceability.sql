-- V2 Sprint A1.1: rework issues created from REWORK_REQUIRED completion reviews

ALTER TABLE issues
    ALTER COLUMN inspection_id DROP NOT NULL;

ALTER TABLE issues
    ADD COLUMN source_completion_review_id BIGINT;

ALTER TABLE issues
    ADD CONSTRAINT uk_issues_source_completion_review_id UNIQUE (source_completion_review_id);

ALTER TABLE issues
    ADD CONSTRAINT fk_issues_source_completion_review
        FOREIGN KEY (source_completion_review_id) REFERENCES completion_reviews (id);

ALTER TABLE issues
    ADD CONSTRAINT chk_issues_has_source CHECK (
        inspection_id IS NOT NULL OR source_completion_review_id IS NOT NULL
    );
