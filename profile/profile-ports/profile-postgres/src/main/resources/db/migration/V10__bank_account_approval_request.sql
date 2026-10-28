
CREATE TABLE IF NOT EXISTS bank_account_approval_request
(
    id              SERIAL PRIMARY KEY,
    bank_account_id BIGINT       NOT NULL REFERENCES bank_account (id) ON DELETE CASCADE,
    user_id         VARCHAR(36)  NOT NULL,
    status          VARCHAR(100) NOT NULL,
    create_date     TIMESTAMP,
    update_date     TIMESTAMP,
    updater         VARCHAR(100),
    description     VARCHAR(255),
    UNIQUE (bank_account_id, status)
);

CREATE INDEX IF NOT EXISTS idx_bank_account_approval_request_user_id
    ON bank_account_approval_request (user_id);

CREATE INDEX IF NOT EXISTS idx_bank_account_approval_request_status_create_date
    ON bank_account_approval_request (status, create_date DESC);
