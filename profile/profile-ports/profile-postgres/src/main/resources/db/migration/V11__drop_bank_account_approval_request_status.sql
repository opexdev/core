ALTER TABLE bank_account_approval_request
    DROP CONSTRAINT IF EXISTS bank_account_approval_request_bank_account_id_status_key;
DROP INDEX IF EXISTS idx_bank_account_approval_request_status_create_date;
ALTER TABLE bank_account_approval_request
    DROP COLUMN IF EXISTS status;
ALTER TABLE bank_account_approval_request
    ADD CONSTRAINT uq_bank_account_approval_request_bank_account UNIQUE (bank_account_id);
