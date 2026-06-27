-- Indexes aligned with list query sort and filter patterns (PostgreSQL)

CREATE INDEX idx_assets_registration_date
    ON assets (registration_date DESC);

CREATE INDEX idx_work_orders_created_at
    ON work_orders (created_at DESC);

CREATE INDEX idx_inspections_created_at
    ON inspections (created_at DESC);

CREATE INDEX idx_notifications_user_created_at
    ON notifications (user_id, created_at DESC);

CREATE INDEX idx_notifications_user_unread_created_at
    ON notifications (user_id, is_read, created_at DESC);

CREATE INDEX idx_operational_documents_asset_uploaded_at
    ON operational_documents (asset_id, uploaded_at DESC);
