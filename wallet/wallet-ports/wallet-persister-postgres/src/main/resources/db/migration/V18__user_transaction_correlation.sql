ALTER TABLE user_transaction ADD COLUMN IF NOT EXISTS operation_id VARCHAR(128);
ALTER TABLE user_transaction ADD COLUMN IF NOT EXISTS financial_action_id VARCHAR(72);

CREATE INDEX IF NOT EXISTS idx_user_transaction_owner_operation
    ON user_transaction (owner_id, operation_id, date, tx_id)
    WHERE operation_id IS NOT NULL;
