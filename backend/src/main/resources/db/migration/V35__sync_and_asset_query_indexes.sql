-- M6.5-STAB-3: composite indexes for mobile sync hot paths and asset-context lookups (PostgreSQL)
-- Backed by InspectionRepository / WorkOrderRepository / MobileService sync scoping queries.

CREATE INDEX IF NOT EXISTS idx_assets_department_id
    ON assets (department_id);

CREATE INDEX IF NOT EXISTS idx_inspections_status
    ON inspections (status);

CREATE INDEX IF NOT EXISTS idx_inspections_assigned_user_updated_at
    ON inspections (assigned_to_user_id, updated_at);

CREATE INDEX IF NOT EXISTS idx_inspections_status_updated_at
    ON inspections (status, updated_at);

CREATE INDEX IF NOT EXISTS idx_inspections_assigned_user_status
    ON inspections (assigned_to_user_id, status);

CREATE INDEX IF NOT EXISTS idx_inspections_asset_status
    ON inspections (asset_id, status);

CREATE INDEX IF NOT EXISTS idx_work_orders_assigned_user_updated_at
    ON work_orders (assigned_to_user_id, updated_at);

CREATE INDEX IF NOT EXISTS idx_work_orders_status_updated_at
    ON work_orders (status, updated_at);

CREATE INDEX IF NOT EXISTS idx_work_orders_assigned_user_status
    ON work_orders (assigned_to_user_id, status);

CREATE INDEX IF NOT EXISTS idx_work_orders_asset_status
    ON work_orders (asset_id, status);

CREATE INDEX IF NOT EXISTS idx_operational_documents_asset_owner_uploaded
    ON operational_documents (asset_id, owner_type, uploaded_at DESC);
