CREATE TABLE request_logs (
                              id             BIGSERIAL PRIMARY KEY,
                              client_key     VARCHAR(255) NOT NULL,
                              endpoint       VARCHAR(255) NOT NULL,
                              blocked_at     TIMESTAMPTZ  NOT NULL,
                              limit_value    INT          NOT NULL,
                              window_seconds INT          NOT NULL
);

CREATE INDEX idx_request_logs_client_key ON request_logs(client_key);
CREATE INDEX idx_request_logs_blocked_at ON request_logs(blocked_at);