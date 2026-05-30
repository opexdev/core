ALTER TABLE wallet_owner add COLUMN external_identifier VARCHAR(100);
CREATE UNIQUE INDEX wallet_owner_external_identifier
    ON wallet_owner (external_identifier)
    WHERE external_identifier IS NOT NULL;