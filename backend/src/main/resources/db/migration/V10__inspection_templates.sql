-- V2 Sprint A2.1: inspection templates as reusable category-level knowledge

CREATE TABLE inspection_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    asset_category_id BIGINT NOT NULL,
    version INTEGER NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT fk_inspection_templates_asset_category
        FOREIGN KEY (asset_category_id) REFERENCES asset_categories (id),
    CONSTRAINT chk_inspection_templates_version_positive CHECK (version > 0)
);

CREATE INDEX idx_inspection_templates_asset_category_id
    ON inspection_templates (asset_category_id);

CREATE INDEX idx_inspection_templates_status
    ON inspection_templates (status);

CREATE INDEX idx_inspection_templates_asset_category_id_status
    ON inspection_templates (asset_category_id, status);
