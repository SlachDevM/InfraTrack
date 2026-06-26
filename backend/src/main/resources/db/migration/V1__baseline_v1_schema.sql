-- InfraTrack V1 baseline schema (PostgreSQL)

CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT uk_departments_name UNIQUE (name)
);

CREATE TABLE asset_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT uk_asset_categories_name UNIQUE (name)
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT,
    fcm_token TEXT,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    department_id BIGINT,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_department FOREIGN KEY (department_id) REFERENCES departments (id)
);

CREATE TABLE account_activation_tokens (
    id BIGSERIAL PRIMARY KEY,
    token TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at BIGINT NOT NULL,
    used_at BIGINT,
    created_at BIGINT NOT NULL,
    CONSTRAINT uk_account_activation_tokens_token UNIQUE (token),
    CONSTRAINT fk_account_activation_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title TEXT,
    message TEXT,
    is_read BOOLEAN NOT NULL,
    created_at BIGINT NOT NULL
);

CREATE TABLE assets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    department_id BIGINT NOT NULL,
    asset_category_id BIGINT NOT NULL,
    location VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    registration_date DATE NOT NULL,
    registered_by_user_id BIGINT NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT fk_assets_department FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT fk_assets_asset_category FOREIGN KEY (asset_category_id) REFERENCES asset_categories (id)
);

CREATE TABLE asset_history_events (
    id BIGSERIAL PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    performed_by_user_id BIGINT NOT NULL,
    event_date DATE NOT NULL,
    created_at BIGINT NOT NULL,
    CONSTRAINT fk_asset_history_events_asset FOREIGN KEY (asset_id) REFERENCES assets (id)
);

CREATE TABLE business_triggers (
    id BIGSERIAL PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    type VARCHAR(255) NOT NULL,
    reason TEXT NOT NULL,
    urgent BOOLEAN NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT fk_business_triggers_asset FOREIGN KEY (asset_id) REFERENCES assets (id)
);

CREATE TABLE inspections (
    id BIGSERIAL PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    business_trigger_id BIGINT NOT NULL,
    assigned_to_user_id BIGINT NOT NULL,
    assigned_by_user_id BIGINT NOT NULL,
    status VARCHAR(255) NOT NULL,
    priority VARCHAR(255) NOT NULL,
    expected_completion_date DATE,
    observed_condition VARCHAR(255),
    observations TEXT,
    issue_identified BOOLEAN NOT NULL,
    completed_at TIMESTAMP(6),
    completed_by_user_id BIGINT,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT fk_inspections_asset FOREIGN KEY (asset_id) REFERENCES assets (id),
    CONSTRAINT fk_inspections_business_trigger FOREIGN KEY (business_trigger_id) REFERENCES business_triggers (id)
);

CREATE TABLE issues (
    id BIGSERIAL PRIMARY KEY,
    inspection_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(255) NOT NULL,
    recorded_by_user_id BIGINT NOT NULL,
    recorded_at TIMESTAMP(6) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT fk_issues_inspection FOREIGN KEY (inspection_id) REFERENCES inspections (id),
    CONSTRAINT fk_issues_asset FOREIGN KEY (asset_id) REFERENCES assets (id)
);

CREATE TABLE operational_decisions (
    id BIGSERIAL PRIMARY KEY,
    issue_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    outcome VARCHAR(255) NOT NULL,
    rationale TEXT NOT NULL,
    decided_by_user_id BIGINT NOT NULL,
    delegated_authority_id BIGINT,
    decided_at TIMESTAMP(6) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT fk_operational_decisions_issue FOREIGN KEY (issue_id) REFERENCES issues (id),
    CONSTRAINT fk_operational_decisions_asset FOREIGN KEY (asset_id) REFERENCES assets (id)
);

CREATE TABLE work_orders (
    id BIGSERIAL PRIMARY KEY,
    operational_decision_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    work_type VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(255) NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    created_at_business_date TIMESTAMP(6) NOT NULL,
    assigned_to_user_id BIGINT,
    assigned_by_user_id BIGINT,
    assigned_at TIMESTAMP(6),
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT fk_work_orders_operational_decision FOREIGN KEY (operational_decision_id) REFERENCES operational_decisions (id),
    CONSTRAINT fk_work_orders_asset FOREIGN KEY (asset_id) REFERENCES assets (id)
);

CREATE TABLE maintenance_activities (
    id BIGSERIAL PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    performed_by_user_id BIGINT NOT NULL,
    completion_notes TEXT NOT NULL,
    completed_at TIMESTAMP(6) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT uk_maintenance_activities_work_order_id UNIQUE (work_order_id),
    CONSTRAINT fk_maintenance_activities_work_order FOREIGN KEY (work_order_id) REFERENCES work_orders (id),
    CONSTRAINT fk_maintenance_activities_asset FOREIGN KEY (asset_id) REFERENCES assets (id)
);

CREATE TABLE completion_reviews (
    id BIGSERIAL PRIMARY KEY,
    maintenance_activity_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    decision VARCHAR(255) NOT NULL,
    review_notes TEXT NOT NULL,
    reviewed_by_user_id BIGINT NOT NULL,
    reviewed_at TIMESTAMP(6) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT uk_completion_reviews_maintenance_activity_id UNIQUE (maintenance_activity_id),
    CONSTRAINT fk_completion_reviews_maintenance_activity FOREIGN KEY (maintenance_activity_id) REFERENCES maintenance_activities (id),
    CONSTRAINT fk_completion_reviews_asset FOREIGN KEY (asset_id) REFERENCES assets (id)
);

CREATE TABLE operational_documents (
    id BIGSERIAL PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    owner_type VARCHAR(255) NOT NULL,
    owner_id BIGINT,
    document_type VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_path VARCHAR(255) NOT NULL,
    document_date DATE,
    uploaded_by_user_id BIGINT NOT NULL,
    uploaded_at TIMESTAMP(6) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT fk_operational_documents_asset FOREIGN KEY (asset_id) REFERENCES assets (id)
);

CREATE TABLE delegated_authorities (
    id BIGSERIAL PRIMARY KEY,
    delegating_manager_user_id BIGINT NOT NULL,
    delegate_manager_user_id BIGINT NOT NULL,
    source_department_id BIGINT NOT NULL,
    target_department_id BIGINT NOT NULL,
    valid_from TIMESTAMP(6) NOT NULL,
    valid_until TIMESTAMP(6) NOT NULL,
    reason TEXT NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP(6),
    revoked_by_user_id BIGINT,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT fk_delegated_authorities_source_department FOREIGN KEY (source_department_id) REFERENCES departments (id),
    CONSTRAINT fk_delegated_authorities_target_department FOREIGN KEY (target_department_id) REFERENCES departments (id)
);
