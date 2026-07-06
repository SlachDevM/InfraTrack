ALTER TABLE mobile_sync_operation
    ADD COLUMN record_state VARCHAR(16) NOT NULL DEFAULT 'RECORDED';

UPDATE mobile_sync_operation
SET record_state = 'RECORDED'
WHERE record_state IS NULL;
