-- V2 Sprint B1.1: preventive maintenance plan domain (configuration only; no execution)

CREATE TABLE preventive_maintenance_plans (
    id                      BIGSERIAL PRIMARY KEY,
    asset_id                BIGINT       NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    description             TEXT,
    status                  VARCHAR(50)  NOT NULL,
    priority                VARCHAR(50)  NOT NULL,
    target_action           VARCHAR(50)  NOT NULL,
    inspection_template_id  BIGINT,
    created_at              BIGINT       NOT NULL,
    updated_at              BIGINT       NOT NULL,
    CONSTRAINT fk_preventive_maintenance_plans_asset
        FOREIGN KEY (asset_id) REFERENCES assets (id),
    CONSTRAINT fk_preventive_maintenance_plans_inspection_template
        FOREIGN KEY (inspection_template_id) REFERENCES inspection_templates (id)
);

CREATE INDEX idx_preventive_maintenance_plans_asset_id ON preventive_maintenance_plans (asset_id);
CREATE INDEX idx_preventive_maintenance_plans_status ON preventive_maintenance_plans (status);

-- Plan-side trigger configuration (distinct from V1 operational business_triggers for UC-006)
CREATE TABLE preventive_plan_business_triggers (
    id                   BIGSERIAL PRIMARY KEY,
    preventive_plan_id   BIGINT       NOT NULL,
    trigger_type         VARCHAR(50)  NOT NULL,
    configuration_json   TEXT         NOT NULL,
    active               BOOLEAN      NOT NULL,
    created_at           BIGINT       NOT NULL,
    updated_at           BIGINT       NOT NULL,
    CONSTRAINT fk_preventive_plan_business_triggers_plan
        FOREIGN KEY (preventive_plan_id) REFERENCES preventive_maintenance_plans (id),
    CONSTRAINT uq_preventive_plan_business_triggers_plan UNIQUE (preventive_plan_id)
);

CREATE INDEX idx_preventive_plan_business_triggers_trigger_type
    ON preventive_plan_business_triggers (trigger_type);
