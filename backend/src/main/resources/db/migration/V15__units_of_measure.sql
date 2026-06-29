-- V2 Sprint A2.3.3: normalized units of measure and answer snapshot finalization

CREATE TABLE units_of_measure (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    quantity_type VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT uk_units_of_measure_code UNIQUE (code)
);

CREATE INDEX idx_units_of_measure_quantity_type ON units_of_measure (quantity_type);
CREATE INDEX idx_units_of_measure_active ON units_of_measure (active);

INSERT INTO units_of_measure (code, symbol, name, quantity_type, active, created_at, updated_at)
VALUES
    ('CELSIUS', '°C', 'Celsius', 'TEMPERATURE', TRUE, 0, 0),
    ('FAHRENHEIT', '°F', 'Fahrenheit', 'TEMPERATURE', TRUE, 0, 0),
    ('BAR', 'bar', 'Bar', 'PRESSURE', TRUE, 0, 0),
    ('PSI', 'psi', 'Pounds per square inch', 'PRESSURE', TRUE, 0, 0),
    ('RPM', 'rpm', 'Revolutions per minute', 'ROTATION', TRUE, 0, 0),
    ('PERCENT', '%', 'Percent', 'RATIO', TRUE, 0, 0),
    ('MILLIMETER', 'mm', 'Millimeter', 'LENGTH', TRUE, 0, 0),
    ('VOLT', 'V', 'Volt', 'ELECTRICAL', TRUE, 0, 0),
    ('AMPERE', 'A', 'Ampere', 'ELECTRICAL', TRUE, 0, 0);

ALTER TABLE inspection_template_questions
    ADD COLUMN unit_of_measure_id BIGINT,
    ADD CONSTRAINT fk_inspection_template_questions_unit_of_measure
        FOREIGN KEY (unit_of_measure_id) REFERENCES units_of_measure (id);

CREATE INDEX idx_inspection_template_questions_unit_of_measure_id
    ON inspection_template_questions (unit_of_measure_id);

-- Backfill from existing free-text unit by symbol match
UPDATE inspection_template_questions q
SET unit_of_measure_id = u.id
FROM units_of_measure u
WHERE q.unit IS NOT NULL
  AND TRIM(q.unit) = u.symbol
  AND q.unit_of_measure_id IS NULL;

-- Backfill by case-insensitive name match
UPDATE inspection_template_questions q
SET unit_of_measure_id = u.id
FROM units_of_measure u
WHERE q.unit IS NOT NULL
  AND LOWER(TRIM(q.unit)) = LOWER(u.name)
  AND q.unit_of_measure_id IS NULL;

-- Preserve unknown legacy units as OTHER reference entries
INSERT INTO units_of_measure (code, symbol, name, quantity_type, active, created_at, updated_at)
SELECT
    'LEGACY_' || UPPER(REGEXP_REPLACE(TRIM(q.unit), '[^A-Za-z0-9]+', '_', 'g')),
    TRIM(q.unit),
    TRIM(q.unit) || ' (legacy)',
    'OTHER',
    TRUE,
    0,
    0
FROM inspection_template_questions q
WHERE q.unit IS NOT NULL
  AND q.unit_of_measure_id IS NULL
GROUP BY TRIM(q.unit);

UPDATE inspection_template_questions q
SET unit_of_measure_id = u.id
FROM units_of_measure u
WHERE q.unit IS NOT NULL
  AND TRIM(q.unit) = u.symbol
  AND u.code LIKE 'LEGACY_%'
  AND q.unit_of_measure_id IS NULL;

ALTER TABLE inspection_answers
    ADD COLUMN unit_code_snapshot VARCHAR(50),
    ADD COLUMN unit_symbol_snapshot VARCHAR(20),
    ADD COLUMN unit_name_snapshot VARCHAR(100),
    ADD COLUMN question_version_snapshot INTEGER;

-- Backfill structured unit snapshots from legacy number_unit_snapshot
UPDATE inspection_answers a
SET unit_symbol_snapshot = a.number_unit_snapshot
WHERE a.number_unit_snapshot IS NOT NULL
  AND a.unit_symbol_snapshot IS NULL;

UPDATE inspection_answers a
SET unit_code_snapshot = u.code,
    unit_name_snapshot = u.name
FROM units_of_measure u
WHERE a.number_unit_snapshot IS NOT NULL
  AND TRIM(a.number_unit_snapshot) = u.symbol
  AND a.unit_code_snapshot IS NULL;

UPDATE inspection_answers a
SET unit_code_snapshot = 'LEGACY_' || UPPER(REGEXP_REPLACE(TRIM(a.number_unit_snapshot), '[^A-Za-z0-9]+', '_', 'g')),
    unit_name_snapshot = TRIM(a.number_unit_snapshot) || ' (legacy)'
WHERE a.number_unit_snapshot IS NOT NULL
  AND a.unit_code_snapshot IS NULL;
