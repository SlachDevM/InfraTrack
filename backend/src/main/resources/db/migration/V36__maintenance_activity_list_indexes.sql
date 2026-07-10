-- Indexes aligned with paginated maintenance activity list queries (PostgreSQL)

CREATE INDEX idx_maintenance_activities_completed_at
    ON maintenance_activities (completed_at DESC);

CREATE INDEX idx_maintenance_activities_asset_completed_at
    ON maintenance_activities (asset_id, completed_at DESC);
