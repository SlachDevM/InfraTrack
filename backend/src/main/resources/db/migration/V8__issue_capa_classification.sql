-- V2 Sprint A1.2: issue type, CAPA metadata, and rework asset history details

ALTER TABLE issues
    ADD COLUMN issue_type VARCHAR(255);

UPDATE issues
SET issue_type = CASE
    WHEN source_completion_review_id IS NOT NULL THEN 'REWORK'
    ELSE 'NORMAL'
END;

ALTER TABLE issues
    ALTER COLUMN issue_type SET NOT NULL;

ALTER TABLE issues
    ADD COLUMN root_cause TEXT;

ALTER TABLE issues
    ADD COLUMN corrective_action TEXT;

ALTER TABLE issues
    ADD COLUMN preventive_action TEXT;

ALTER TABLE asset_history_events
    ADD COLUMN details TEXT;
