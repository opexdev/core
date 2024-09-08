CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS address_types
(
    id            SERIAL PRIMARY KEY,
    address_type  VARCHAR(20) NOT NULL,
    address_regex VARCHAR(72) NOT NULL,
    memo_regex    VARCHAR(72)
);

CREATE TABLE IF NOT EXISTS assigned_addresses
(
    id           SERIAL PRIMARY KEY,
    uuid         VARCHAR(72) NOT NULL,
    address      VARCHAR(72) NOT NULL,
    memo         VARCHAR(72) NOT NULL,
    addr_type_id INTEGER     NOT NULL REFERENCES address_types (id),
    assigned_date TIMESTAMP,
    revoked_date TIMESTAMP,
    status VARCHAR(25),
    exp_time TIMESTAMP,
    UNIQUE (address, memo, exp_time)
);

ALTER TABLE assigned_addresses ADD COLUMN IF NOT EXISTS assigned_date TIMESTAMP;
ALTER TABLE assigned_addresses ADD COLUMN IF NOT EXISTS revoked_date TIMESTAMP;
ALTER TABLE assigned_addresses ADD COLUMN IF NOT EXISTS exp_time TIMESTAMP;
ALTER TABLE assigned_addresses ADD COLUMN IF NOT EXISTS status VARCHAR(25);




CREATE TABLE IF NOT EXISTS reserved_addresses
(
    id           SERIAL PRIMARY KEY,
    address      VARCHAR(72) NOT NULL,
    memo         VARCHAR(72) NOT NULL,
    address_type INTEGER     NOT NULL REFERENCES address_types (id),
    UNIQUE (address, memo)
);

CREATE TABLE IF NOT EXISTS chains
(
    name VARCHAR(72) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS assigned_address_chains
(
    id                  SERIAL      PRIMARY KEY,
    assigned_address_id INTEGER     NOT NULL REFERENCES assigned_addresses (id),
    chain               VARCHAR(72) NOT NULL REFERENCES chains (name)
);

CREATE TABLE IF NOT EXISTS chain_address_types
(
    id           SERIAL      PRIMARY KEY,
    chain_name   VARCHAR(72) NOT NULL REFERENCES chains (name),
    addr_type_id INTEGER     NOT NULL REFERENCES address_types (id),
    UNIQUE (chain_name, addr_type_id)
);

-- CREATE TABLE IF NOT EXISTS currency
-- (
--     symbol VARCHAR(72) PRIMARY KEY,
--     name   VARCHAR(72) NOT NULL
-- );

CREATE TABLE IF NOT EXISTS currency_implementations
(
    id                    SERIAL PRIMARY KEY,
    currency_symbol       VARCHAR(72) NOT NULL ,
    implementation_symbol VARCHAR(72) NOT NULL,
    gateway_uuid             VARCHAR(256) NOT NULL UNIQUE DEFAULT  uuid_generate_v4(),
    chain                 VARCHAR(72) NOT NULL REFERENCES chains (name),
    is_token                 BOOLEAN     NOT NULL,
    token_address         VARCHAR(72),
    token_name            VARCHAR(72),
    withdraw_allowed      BOOLEAN     NOT NULL,
    deposit_allowed      BOOLEAN     NOT NULL,
    withdraw_fee          DECIMAL     NOT NULL,
    withdraw_min          DECIMAL     NOT NULL,
    withdraw_max          DECIMAL     NOT NULL,
    deposit_min          DECIMAL     NOT NULL,
    deposit_max          DECIMAL     NOT NULL,
    decimal               INTEGER     NOT NULL,
    is_active             BOOLEAN     NOT NULL DEFAULT TRUE,
    UNIQUE (currency_symbol, chain, implementation_symbol)
);
ALTER TABLE currency_implementations RENAME TO currency_on_chain_gateway;
ALTER TABLE currency_on_chain_gateway DROP CONSTRAINT  IF EXISTS currency_implementations_currency_symbol_fkey;
ALTER TABLE currency_on_chain_gateway ADD COLUMN IF NOT EXISTS impl_uuid VARCHAR(256) NOT NULL UNIQUE DEFAULT  uuid_generate_v4();
ALTER TABLE currency_on_chain_gateway ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL  DEFAULT TRUE;
ALTER TABLE currency_on_chain_gateway ADD COLUMN IF NOT EXISTS deposit_allowed BOOLEAN NOT NULL  DEFAULT TRUE;
ALTER TABLE currency_on_chain_gateway ADD COLUMN IF NOT EXISTS withdraw_max DECIMAL ;
ALTER TABLE currency_on_chain_gateway ADD COLUMN IF NOT EXISTS deposit_max DECIMAL ;
ALTER TABLE currency_on_chain_gateway ADD COLUMN IF NOT EXISTS deposit_min DECIMAL ;
-- ALTER TABLE currency_on_chain_gateway RENAME COLUMN withdraw_enabled to withdraw_allowed ;
-- ALTER TABLE currency_on_chain_gateway RENAME COLUMN token to is_token ;
ALTER TABLE currency_on_chain_gateway RENAME COLUMN impl_uuid to gateway_uuid ;





CREATE TABLE IF NOT EXISTS deposits
(
    id               SERIAL       PRIMARY KEY,
    hash             VARCHAR(100) UNIQUE NOT NULL,
    chain            VARCHAR(72)  NOT NULL REFERENCES chains (name),
    token            BOOLEAN      NOT NULL,
    token_address    VARCHAR(72),
    amount           DECIMAL      NOT NULL,
    depositor        VARCHAR(72)  NOT NULL,
    depositor_memo   VARCHAR(72)
);



