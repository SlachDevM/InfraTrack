-- V2 Sprint A2.2.1: stable business codes for inspection template questions

ALTER TABLE inspection_template_questions
    ADD COLUMN code VARCHAR(100);

DO $$
DECLARE
    rec RECORD;
    base_code VARCHAR(100);
    final_code VARCHAR(100);
    suffix INTEGER;
BEGIN
    FOR rec IN
        SELECT id, inspection_template_id, question_text
        FROM inspection_template_questions
        ORDER BY inspection_template_id, display_order, id
    LOOP
        base_code := upper(trim(both '_' from regexp_replace(
            regexp_replace(trim(rec.question_text), '[^a-zA-Z0-9]+', '_', 'g'),
            '_+', '_', 'g'
        )));

        IF base_code = '' THEN
            base_code := 'QUESTION';
        END IF;

        IF base_code !~ '^[A-Z]' THEN
            base_code := 'Q_' || base_code;
        END IF;

        IF length(base_code) > 100 THEN
            base_code := rtrim(substring(base_code from 1 for 100), '_');
        END IF;

        final_code := base_code;
        suffix := 2;

        WHILE EXISTS (
            SELECT 1
            FROM inspection_template_questions existing
            WHERE existing.inspection_template_id = rec.inspection_template_id
              AND existing.code = final_code
              AND existing.id <> rec.id
        ) LOOP
            final_code := base_code || '_' || suffix::text;
            suffix := suffix + 1;
        END LOOP;

        UPDATE inspection_template_questions
        SET code = final_code
        WHERE id = rec.id;
    END LOOP;
END $$;

ALTER TABLE inspection_template_questions
    ALTER COLUMN code SET NOT NULL;

CREATE UNIQUE INDEX idx_inspection_template_questions_template_id_code
    ON inspection_template_questions (inspection_template_id, code);
