-- V2.1.0 Sprint C5: per-user Operations Intelligence dashboard presentation preferences

CREATE TABLE dashboard_preferences (
    id                              BIGSERIAL PRIMARY KEY,
    user_id                         BIGINT       NOT NULL UNIQUE,
    show_overview_widget            BOOLEAN      NOT NULL,
    show_attention_widget           BOOLEAN      NOT NULL,
    show_trend_widget               BOOLEAN      NOT NULL,
    show_recent_activity_widget     BOOLEAN      NOT NULL,
    show_quick_navigation_widget    BOOLEAN      NOT NULL,
    default_trend_range             VARCHAR(50)  NOT NULL,
    widget_order_json               TEXT         NOT NULL,
    created_at                      BIGINT       NOT NULL,
    updated_at                      BIGINT       NOT NULL
);

CREATE INDEX idx_dashboard_preferences_user_id ON dashboard_preferences (user_id);
