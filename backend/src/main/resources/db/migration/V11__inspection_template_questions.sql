-- V2 Sprint A2.2: checklist questions on inspection templates

CREATE TABLE inspection_template_questions (
    id BIGSERIAL PRIMARY KEY,
    inspection_template_id BIGINT NOT NULL,
    question_text VARCHAR(2000) NOT NULL,
    help_text VARCHAR(4000),
    question_type VARCHAR(255) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT fk_inspection_template_questions_template
        FOREIGN KEY (inspection_template_id) REFERENCES inspection_templates (id),
    CONSTRAINT chk_inspection_template_questions_display_order_positive CHECK (display_order > 0)
);

CREATE INDEX idx_inspection_template_questions_template_id
    ON inspection_template_questions (inspection_template_id);

CREATE INDEX idx_inspection_template_questions_template_id_display_order
    ON inspection_template_questions (inspection_template_id, display_order);

CREATE INDEX idx_inspection_template_questions_template_id_active
    ON inspection_template_questions (inspection_template_id, active);
