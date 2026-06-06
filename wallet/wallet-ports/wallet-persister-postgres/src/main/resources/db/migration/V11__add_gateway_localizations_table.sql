CREATE TABLE IF NOT EXISTS currency_off_chain_gateway_localization
(
    id                   SERIAL PRIMARY KEY,
    gateway_id           BIGINT      NOT NULL REFERENCES currency_off_chain_gateway (id) ON DELETE CASCADE,
    deposit_description  TEXT,
    withdraw_description TEXT,
    language             VARCHAR(10) NOT NULL,
    UNIQUE (gateway_id, language)
);

INSERT INTO currency_off_chain_gateway_localization (gateway_id,
                                                     deposit_description,
                                                     withdraw_description,
                                                     language)
SELECT id, deposit_description, withdraw_description, 'EN'
FROM currency_off_chain_gateway;

ALTER TABLE currency_off_chain_gateway
    DROP COLUMN deposit_description,
    DROP COLUMN withdraw_description;