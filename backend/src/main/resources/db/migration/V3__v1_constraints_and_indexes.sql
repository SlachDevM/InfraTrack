-- V1 business invariants and query indexes (PostgreSQL)

ALTER TABLE issues
    ADD CONSTRAINT uk_issues_inspection_id UNIQUE (inspection_id);

ALTER TABLE operational_decisions
    ADD CONSTRAINT uk_operational_decisions_issue_id UNIQUE (issue_id);

ALTER TABLE work_orders
    ADD CONSTRAINT uk_work_orders_operational_decision_id UNIQUE (operational_decision_id);

CREATE INDEX idx_asset_history_events_asset_event_created
    ON asset_history_events (asset_id, event_date DESC, created_at DESC);

CREATE INDEX idx_operational_documents_asset_id
    ON operational_documents (asset_id);

CREATE INDEX idx_work_orders_status
    ON work_orders (status);

CREATE INDEX idx_work_orders_assigned_to_user_id
    ON work_orders (assigned_to_user_id);

CREATE INDEX idx_inspections_business_trigger_status
    ON inspections (business_trigger_id, status);
