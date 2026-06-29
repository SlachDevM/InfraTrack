-- V2 Sprint A2.3.2: CHOICE options and NUMBER constraints for inspection template questions

ALTER TABLE inspection_template_questions
    ADD COLUMN unit VARCHAR(50),
    ADD COLUMN min_value NUMERIC(19, 4),
    ADD COLUMN max_value NUMERIC(19, 4),
    ADD COLUMN decimal_places INTEGER;

CREATE TABLE inspection_template_question_choices (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    code VARCHAR(100) NOT NULL,
    label VARCHAR(500) NOT NULL,
    display_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT fk_inspection_template_question_choices_question
        FOREIGN KEY (question_id) REFERENCES inspection_template_questions (id),
    CONSTRAINT uk_inspection_template_question_choices_question_code
        UNIQUE (question_id, code),
    CONSTRAINT chk_inspection_template_question_choices_display_order
        CHECK (display_order > 0)
);

CREATE INDEX idx_inspection_template_question_choices_question_id
    ON inspection_template_question_choices (question_id);

ALTER TABLE inspection_answers
    ADD COLUMN choice_code_value VARCHAR(100),
    ADD COLUMN choice_label_snapshot VARCHAR(500),
    ADD COLUMN number_unit_snapshot VARCHAR(50),
    ADD COLUMN number_min_snapshot NUMERIC(19, 4),
    ADD COLUMN number_max_snapshot NUMERIC(19, 4),
    ADD COLUMN decimal_places_snapshot INTEGER;

ALTER TABLE inspection_answers
    DROP CONSTRAINT chk_inspection_answers_single_value;

ALTER TABLE inspection_answers
    ADD CONSTRAINT chk_inspection_answers_single_value CHECK (
        (CASE WHEN boolean_value IS NOT NULL THEN 1 ELSE 0 END)
        + (CASE WHEN text_value IS NOT NULL THEN 1 ELSE 0 END)
        + (CASE WHEN number_value IS NOT NULL THEN 1 ELSE 0 END)
        + (CASE WHEN choice_code_value IS NOT NULL THEN 1 ELSE 0 END) = 1
    );
