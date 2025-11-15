ALTER TABLE profile_approval_request
    ADD COLUMN user_id VARCHAR(36);

UPDATE profile_approval_request par
SET user_id = p.user_id
FROM profile p
WHERE p.id = par.profile_id;

ALTER TABLE profile_approval_request
    DROP COLUMN profile_id;