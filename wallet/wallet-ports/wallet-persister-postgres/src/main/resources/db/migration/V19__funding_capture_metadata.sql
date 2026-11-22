ALTER TABLE transaction ADD COLUMN IF NOT EXISTS operation_id VARCHAR(128);
ALTER TABLE transaction ADD COLUMN IF NOT EXISTS transfer_method VARCHAR(36);
ALTER TABLE transaction ADD COLUMN IF NOT EXISTS applicator VARCHAR(128);
ALTER TABLE deposits ADD COLUMN IF NOT EXISTS gateway_uuid VARCHAR(128);
ALTER TABLE withdraws ADD COLUMN IF NOT EXISTS gateway_uuid VARCHAR(128);
CREATE INDEX IF NOT EXISTS idx_transaction_operation_id ON transaction (operation_id);
