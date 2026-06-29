-- V2 Sprint A2.3.1: inspection template link and structured inspection answers

ALTER TABLE inspections
    ADD COLUMN inspection_template_id BIGINT,
    ADD CONSTRAINT fk_inspections_inspection_template
        FOREIGN KEY (inspection_template_id) REFERENCES inspection_templates (id);

CREATE INDEX idx_inspections_inspection_template_id
    ON inspections (inspection_template_id);

CREATE TABLE inspection_answers (
    id BIGSERIAL PRIMARY KEY,
    inspection_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    question_code_snapshot VARCHAR(100) NOT NULL,
    question_text_snapshot VARCHAR(2000) NOT NULL,
    question_type_snapshot VARCHAR(255) NOT NULL,
    boolean_value BOOLEAN,
    text_value VARCHAR(4000),
    number_value NUMERIC(19, 4),
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT fk_inspection_answers_inspection
        FOREIGN KEY (inspection_id) REFERENCES inspections (id),
    CONSTRAINT fk_inspection_answers_question
        FOREIGN KEY (question_id) REFERENCES inspection_template_questions (id),
    CONSTRAINT uk_inspection_answers_inspection_question
        UNIQUE (inspection_id, question_id),
    CONSTRAINT chk_inspection_answers_single_value CHECK (
        (CASE WHEN boolean_value IS NOT NULL THEN 1 ELSE 0 END)
        + (CASE WHEN text_value IS NOT NULL THEN 1 ELSE 0 END)
        + (CASE WHEN number_value IS NOT NULL THEN 1 ELSE 0 END) = 1
    )
);

CREATE INDEX idx_inspection_answers_inspection_id
    ON inspection_answers (inspection_id);

CREATE INDEX idx_inspection_answers_question_id
    ON inspection_answers (question_id);

CREATE INDEX idx_inspection_answers_question_code_snapshot
    ON inspection_answers (question_code_snapshot);
