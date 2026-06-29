-- V2 Sprint A3.5: Decision Assistant — suggestion review and issue linkage

ALTER TABLE suggested_actions
    ADD COLUMN confidence VARCHAR(50) NOT NULL DEFAULT 'LOW',
    ADD COLUMN rule_evaluation_result_id BIGINT,
    ADD COLUMN rejection_reason TEXT,
    ADD COLUMN dismiss_comment TEXT,
    ADD COLUMN created_issue_id BIGINT,
    ADD COLUMN decided_by_user_id BIGINT,
    ADD COLUMN decided_at BIGINT,
    ADD CONSTRAINT fk_suggested_actions_rule_evaluation_result
        FOREIGN KEY (rule_evaluation_result_id) REFERENCES rule_evaluation_results (id),
    ADD CONSTRAINT fk_suggested_actions_created_issue
        FOREIGN KEY (created_issue_id) REFERENCES issues (id);

CREATE INDEX idx_suggested_actions_rule_evaluation_result_id
    ON suggested_actions (rule_evaluation_result_id);

CREATE INDEX idx_suggested_actions_created_issue_id
    ON suggested_actions (created_issue_id);

ALTER TABLE issues
    ADD COLUMN suggested_action_id BIGINT,
    ADD COLUMN rule_evaluation_report_id BIGINT,
    ADD CONSTRAINT fk_issues_suggested_action
        FOREIGN KEY (suggested_action_id) REFERENCES suggested_actions (id),
    ADD CONSTRAINT fk_issues_rule_evaluation_report
        FOREIGN KEY (rule_evaluation_report_id) REFERENCES rule_evaluation_reports (id);

CREATE INDEX idx_issues_suggested_action_id ON issues (suggested_action_id);
CREATE INDEX idx_issues_rule_evaluation_report_id ON issues (rule_evaluation_report_id);
