-- V2 Sprint A3.2: rule priority and disabled reason for decision rule evaluation

ALTER TABLE inspection_template_question_rules
    ADD COLUMN priority INTEGER NOT NULL DEFAULT 100,
    ADD COLUMN disabled_reason TEXT;

ALTER TABLE inspection_template_question_rules
    ADD CONSTRAINT chk_inspection_template_question_rules_priority_positive
        CHECK (priority > 0);

CREATE INDEX idx_inspection_template_question_rules_question_active_priority
    ON inspection_template_question_rules (question_id, active, priority);
