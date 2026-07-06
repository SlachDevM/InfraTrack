-- V2.5-STAB-2: indexes for mobile inspection sync hot paths (PostgreSQL)

CREATE INDEX IF NOT EXISTS idx_inspections_assigned_to_user_id
    ON inspections (assigned_to_user_id);

CREATE INDEX IF NOT EXISTS idx_inspections_updated_at
    ON inspections (updated_at);
