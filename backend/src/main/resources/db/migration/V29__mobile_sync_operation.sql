CREATE TABLE mobile_sync_operation (
    operation_id       VARCHAR(128) PRIMARY KEY,
    user_id            BIGINT       NOT NULL,
    entity_type        VARCHAR(64)  NOT NULL,
    entity_id          BIGINT,
    operation_type     VARCHAR(128) NOT NULL,
    protocol_version   INTEGER      NOT NULL,
    processed_at       TIMESTAMPTZ  NOT NULL,
    response_status    VARCHAR(32)  NOT NULL,
    response_message   VARCHAR(1024),
    server_updated_at  TIMESTAMPTZ,
    conflict_type      VARCHAR(64),
    conflict_message   VARCHAR(1024)
);

CREATE INDEX idx_mobile_sync_operation_processed_at ON mobile_sync_operation (processed_at);

CREATE INDEX idx_mobile_sync_operation_user_id ON mobile_sync_operation (user_id);
