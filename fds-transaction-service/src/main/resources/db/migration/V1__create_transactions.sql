CREATE TABLE transactions (
    id                    BIGSERIAL PRIMARY KEY,
    transaction_id        VARCHAR(36) UNIQUE NOT NULL,
    user_id               VARCHAR(20) NOT NULL,
    encrypted_card_number VARCHAR(256) NOT NULL,
    masked_card_number    VARCHAR(20) NOT NULL,
    amount                DECIMAL(18,2) NOT NULL,
    currency              VARCHAR(3) NOT NULL,
    merchant_name         VARCHAR(100) NOT NULL,
    merchant_category     VARCHAR(30) NOT NULL,
    country               VARCHAR(3) NOT NULL,
    city                  VARCHAR(50) NOT NULL,
    latitude              DOUBLE PRECISION NOT NULL,
    longitude             DOUBLE PRECISION NOT NULL,
    status                VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    risk_level            VARCHAR(10),
    risk_score            INTEGER,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tx_transaction_id ON transactions(transaction_id);
CREATE INDEX idx_tx_user_id ON transactions(user_id);
CREATE INDEX idx_tx_status ON transactions(status);
CREATE INDEX idx_tx_created_at ON transactions(created_at DESC);
CREATE INDEX idx_tx_user_created ON transactions(user_id, created_at DESC);
