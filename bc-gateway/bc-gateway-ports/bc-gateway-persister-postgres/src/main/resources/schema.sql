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
    id            SERIAL PRIMARY KEY,
    uuid          VARCHAR(72) NOT NULL,
    address       VARCHAR(72) NOT NULL,
    memo          VARCHAR(72) NOT NULL,
    addr_type_id  INTEGER     NOT NULL REFERENCES address_types (id),
    assigned_date TIMESTAMP,
    revoked_date  TIMESTAMP,
    status        VARCHAR(25),
    exp_time      TIMESTAMP,
    UNIQUE (address, memo, exp_time)
);



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
    id                  SERIAL PRIMARY KEY,
    assigned_address_id INTEGER     NOT NULL REFERENCES assigned_addresses (id),
    chain               VARCHAR(72) NOT NULL REFERENCES chains (name)
);

CREATE TABLE IF NOT EXISTS chain_address_types
(
    id           SERIAL PRIMARY KEY,
    chain_name   VARCHAR(72) NOT NULL REFERENCES chains (name),
    addr_type_id INTEGER     NOT NULL REFERENCES address_types (id),
    UNIQUE (chain_name, addr_type_id)
);

-- CREATE TABLE IF NOT EXISTS currency
-- (
--     symbol VARCHAR(72) PRIMARY KEY,
--     name   VARCHAR(72) NOT NULL
-- );

CREATE TABLE IF NOT EXISTS currency_on_chain_gateway
(
    id                    SERIAL PRIMARY KEY,
    currency_symbol       VARCHAR(72)  NOT NULL,
    implementation_symbol VARCHAR(72)  NOT NULL,
    gateway_uuid          VARCHAR(256) NOT NULL UNIQUE DEFAULT uuid_generate_v4(),
    chain                 VARCHAR(72)  NOT NULL REFERENCES chains (name),
    is_token              BOOLEAN      NOT NULL,
    token_address         VARCHAR(72),
    token_name            VARCHAR(72),
    withdraw_allowed      BOOLEAN      NOT NULL,
    deposit_allowed       BOOLEAN      NOT NULL,
    withdraw_fee          DECIMAL      NOT NULL,
    withdraw_min          DECIMAL      NOT NULL,
    withdraw_max          DECIMAL      NOT NULL,
    deposit_min           DECIMAL      NOT NULL,
    deposit_max           DECIMAL      NOT NULL,
    decimal               INTEGER      NOT NULL,
    is_deposit_active        BOOLEAN      NOT NULL        DEFAULT TRUE,
    is_withdraw_active        BOOLEAN      NOT NULL        DEFAULT TRUE,
    description            TEXT,
    UNIQUE (currency_symbol, chain, implementation_symbol)
);



CREATE TABLE IF NOT EXISTS deposits
(
    id             SERIAL PRIMARY KEY,
    hash           VARCHAR(100) UNIQUE NOT NULL,
    chain          VARCHAR(72)         NOT NULL REFERENCES chains (name),
    token          BOOLEAN             NOT NULL,
    token_address  VARCHAR(72),
    amount         DECIMAL             NOT NULL,
    depositor      VARCHAR(72)         NOT NULL,
    depositor_memo VARCHAR(72)
);

DO
$$
BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_name = 'currency_on_chain_gateway' AND column_name = 'is_deposit_active') THEN ALTER TABLE currency_on_chain_gateway
    ADD COLUMN is_deposit_active Boolean NOT NULL DEFAULT TRUE;
END IF;
        IF EXISTS (SELECT 1
                               FROM information_schema.columns
                               WHERE table_name = 'currency_on_chain_gateway' AND column_name = 'is_active') THEN ALTER TABLE currency_on_chain_gateway
    RENAME COLUMN is_active TO is_withdraw_active;
END IF;
        IF NOT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_name = 'currency_on_chain_gateway' AND column_name = 'description') THEN ALTER TABLE currency_on_chain_gateway
    ADD COLUMN description TEXT;
END IF;
END
$$;


