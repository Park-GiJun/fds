CREATE TABLE detection_results (
    id               BIGSERIAL PRIMARY KEY,
    detection_id     VARCHAR(36) UNIQUE NOT NULL,
    transaction_id   VARCHAR(36) NOT NULL,
    user_id          VARCHAR(20) NOT NULL,
    risk_level       VARCHAR(10) NOT NULL,
    risk_score       INTEGER NOT NULL CHECK (risk_score BETWEEN 0 AND 100),
    triggered_rules  JSONB NOT NULL DEFAULT '[]',
    detected_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_detection_transaction
        FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id)
);

CREATE INDEX idx_det_transaction_id ON detection_results(transaction_id);
CREATE INDEX idx_det_user_id ON detection_results(user_id);
CREATE INDEX idx_det_detected_at ON detection_results(detected_at DESC);
