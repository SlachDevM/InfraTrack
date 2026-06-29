-- V2 Sprint A3.1: decision rules on inspection template questions (storage only)

CREATE TABLE inspection_template_question_rules (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    rule_code VARCHAR(100) NOT NULL,
    rule_name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    condition_type VARCHAR(50) NOT NULL,
    operator VARCHAR(50) NOT NULL,
    comparison_value VARCHAR(500),
    action_type VARCHAR(50) NOT NULL,
    action_payload TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT fk_inspection_template_question_rules_question
        FOREIGN KEY (question_id) REFERENCES inspection_template_questions (id),
    CONSTRAINT uk_inspection_template_question_rules_question_code
        UNIQUE (question_id, rule_code)
);

CREATE INDEX idx_inspection_template_question_rules_question_id
    ON inspection_template_question_rules (question_id);

CREATE INDEX idx_inspection_template_question_rules_question_active
    ON inspection_template_question_rules (question_id, active);

CREATE INDEX idx_inspection_template_question_rules_question_code
    ON inspection_template_question_rules (question_id, rule_code);
