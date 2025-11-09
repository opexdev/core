ALTER TABLE profile
    DROP COLUMN IF EXISTS verification_status;

ALTER TABLE profile_history
    DROP COLUMN IF EXISTS verification_status;