-- V2 Sprint B1.2: plan identity and trigger definition validation support

ALTER TABLE preventive_maintenance_plans
    ADD COLUMN plan_code VARCHAR(100) NOT NULL DEFAULT 'LEGACY_PLAN',
    ADD COLUMN version INTEGER NOT NULL DEFAULT 1;

ALTER TABLE preventive_maintenance_plans
    ALTER COLUMN plan_code DROP DEFAULT;

ALTER TABLE preventive_maintenance_plans
    ADD CONSTRAINT uq_preventive_maintenance_plans_plan_code UNIQUE (plan_code),
    ADD CONSTRAINT chk_preventive_maintenance_plans_version_positive CHECK (version > 0);
