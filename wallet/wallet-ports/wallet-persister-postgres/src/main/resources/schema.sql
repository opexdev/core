
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS currency
(
    symbol            VARCHAR(25) PRIMARY KEY,
    name              VARCHAR(25),
    precision         DECIMAL NOT NULL,
    title             VARCHAR(25),
    alias             VARCHAR(25),
    icon              TEXT,
    last_update_date  TIMESTAMP,
    create_date       TIMESTAMP,
    is_transitive     BOOLEAN NOT NULL DEFAULT FALSE,
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    sign              VARCHAR(25),
    description       TEXT,
    short_description TEXT,
    uuid              VARCHAR(256) NOT NULL DEFAULT  uuid_generate_v4(),
    external_url           VARCHAR(255));
ALTER TABLE currency ADD COLUMN IF NOT EXISTS title VARCHAR(25);
ALTER TABLE currency ADD COLUMN IF NOT EXISTS alias VARCHAR(25);
ALTER TABLE currency  ADD COLUMN IF NOT EXISTS icon TEXT;
ALTER TABLE currency  ADD COLUMN IF NOT EXISTS last_update_date TIMESTAMP;
ALTER TABLE currency ADD COLUMN IF NOT EXISTS create_date TIMESTAMP;
ALTER TABLE currency  ADD COLUMN IF NOT EXISTS is_transitive BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE currency ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE currency  ADD COLUMN IF NOT EXISTS sign VARCHAR(25);
ALTER TABLE currency ADD COLUMN IF NOT EXISTS uuid VARCHAR(256) NOT NULL DEFAULT  uuid_generate_v4();
ALTER TABLE currency  ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE currency ADD COLUMN IF NOT EXISTS short_description TEXT;
ALTER TABLE currency  ADD COLUMN IF NOT EXISTS external_url    VARCHAR(255);
-- ALTER TABLE currency  ADD COLUMN IF NOT EXISTS is_crypto_currency BOOLEAN DEFAULT FALSE;
-- ALTER TABLE currency DROP COLUMN withdraw_allowed;
-- ALTER TABLE currency DROP COLUMN deposit_allowed;
-- ALTER TABLE currency DROP COLUMN withdraw_fee;




CREATE TABLE IF NOT EXISTS wallet_owner
(
    id               SERIAL PRIMARY KEY,
    uuid             VARCHAR(36) NOT NULL UNIQUE,
    title            VARCHAR(70) NOT NULL,
    level            VARCHAR(10) NOT NULL,
    trade_allowed    BOOLEAN     NOT NULL DEFAULT TRUE,
    withdraw_allowed BOOLEAN     NOT NULL DEFAULT TRUE,
    deposit_allowed  BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS wallet
(
    id          SERIAL PRIMARY KEY,
    owner       INTEGER     NOT NULL REFERENCES wallet_owner (id),
    wallet_type VARCHAR(10) NOT NULL,
    currency    VARCHAR(25) NOT NULL REFERENCES currency (symbol),
    balance     DECIMAL     NOT NULL,
    UNIQUE (owner, wallet_type, currency)
);

ALTER TABLE wallet
    ADD COLUMN IF NOT EXISTS version INTEGER;

CREATE TABLE IF NOT EXISTS transaction
(
    id                SERIAL PRIMARY KEY,
    source_wallet     INTEGER     NOT NULL REFERENCES wallet (id),
    dest_wallet       INTEGER     NOT NULL REFERENCES wallet (id),
    source_amount     DECIMAL     NOT NULL,
    dest_amount       DECIMAL     NOT NULL,
    description       TEXT,
    transfer_ref      TEXT        NOT NULL UNIQUE,
    transfer_category VARCHAR(36) NOT NULL DEFAULT 'NO_CATEGORY',
    transaction_date  TIMESTAMP   NOT NULL DEFAULT CURRENT_DATE
);

ALTER TABLE transaction
    ADD COLUMN IF NOT EXISTS transfer_category VARCHAR(36) NOT NULL DEFAULT 'NO_CATEGORY';
ALTER TABLE transaction
    ALTER COLUMN transfer_ref SET NOT NULL;

CREATE TABLE IF NOT EXISTS user_transaction
(
    id             SERIAL PRIMARY KEY,
    uuid           VARCHAR(36)  NOT NULL UNIQUE,
    owner_id       INTEGER      NOT NULL REFERENCES wallet_owner (id),
    tx_id          INTEGER      NOT NULL REFERENCES transaction (id),
    currency       VARCHAR(25)  NOT NULL REFERENCES currency (symbol),
    balance        DECIMAL      NOT NULL,
    balance_change DECIMAL      NOT NULL,
    category       VARCHAR(128) NOT NULL,
    description    TEXT,
    date           TIMESTAMP    NOT NULL DEFAULT CURRENT_DATE
);
CREATE INDEX IF NOT EXISTS idx_user_transaction_category ON user_transaction (category);

CREATE TABLE IF NOT EXISTS wallet_limits
(
    id            SERIAL PRIMARY KEY,
    level         VARCHAR(10),
    owner         INTEGER REFERENCES wallet_owner (id),
    action        VARCHAR(25),
    currency      VARCHAR(25) REFERENCES currency (symbol),
    wallet_type   VARCHAR(10),
    wallet_id     INTEGER REFERENCES wallet (id),
    daily_total   DECIMAL,
    daily_count   INTEGER,
    monthly_total DECIMAL,
    monthly_count INTEGER
);

CREATE TABLE IF NOT EXISTS wallet_config
(
    name          VARCHAR(20) PRIMARY KEY,
    main_currency VARCHAR(25) NOT NULL REFERENCES currency (symbol)
);

CREATE TABLE IF NOT EXISTS withdraws
(
    id                   SERIAL PRIMARY KEY,
    uuid                 VARCHAR(36) NOT NULL,
    req_transaction_id   VARCHAR(20) NOT NULL UNIQUE,
    final_transaction_id VARCHAR(20) UNIQUE,
    currency             VARCHAR(20) NOT NULL REFERENCES currency (symbol),
    wallet               INTEGER     NOT NULL REFERENCES wallet (id),
    amount               DECIMAL     NOT NULL,
    applied_fee          DECIMAL,
    dest_amount          DECIMAL,
    dest_symbol          VARCHAR(20),
    dest_network         VARCHAR(80),
    dest_address         VARCHAR(80),
    dest_notes           TEXT,
    dest_transaction_ref VARCHAR(100),
    description          TEXT,
    status_reason        TEXT,
    status               VARCHAR(20),
    create_date          TIMESTAMP   NOT NULL,
    last_update_date          TIMESTAMP,
    applicator            VARCHAR(80)
);

CREATE TABLE IF NOT EXISTS rate
(
    id               SERIAL PRIMARY KEY,
    source_symbol    VARCHAR(25) NOT NULL REFERENCES currency (symbol),
    dest_symbol      VARCHAR(25) NOT NULL REFERENCES currency (symbol),
    rate             DECIMAL,
    last_update_date TIMESTAMP,
    create_date      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS forbidden_pair
(
    id               SERIAL PRIMARY KEY,
    source_symbol    VARCHAR(25) NOT NULL REFERENCES currency (symbol),
    dest_symbol      VARCHAR(25) NOT NULL REFERENCES currency (symbol),
    last_update_date TIMESTAMP,
    create_date      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reserved_transfer
(
    id                   SERIAL PRIMARY KEY,
    reserve_number       VARCHAR(100) NOT NULL UNIQUE,
    source_symbol        VARCHAR(25)  NOT NULL REFERENCES currency (symbol),
    dest_symbol          VARCHAR(25)  NOT NULL REFERENCES currency (symbol),
    sender_wallet_type   VARCHAR(25)  NOT NULL,
    sender_uuid          VARCHAR(100) NOT NULL,
    receiver_wallet_type VARCHAR(25)  NOT NULL,
    receiver_uuid        VARCHAR(100) NOT NULL,
    source_amount        DECIMAL      NOT NULL,
    reserved_dest_amount DECIMAL      NOT NULL,
    reserve_date         TIMESTAMP,
    exp_date             TIMESTAMP,
    status               VARCHAR(25),
    rate                 DECIMAL
);

CREATE TABLE IF NOT EXISTS wallet_stat_exclusion
(
    id        SERIAL PRIMARY KEY,
    wallet_id INTEGER NOT NULL UNIQUE REFERENCES wallet (id)
);


CREATE TABLE IF NOT EXISTS deposits
(
    id              SERIAL PRIMARY KEY,
    uuid            VARCHAR(255) NOT NULL,
    duid            VARCHAR(255) NOT NULL,
    currency        VARCHAR(255) NOT NULL REFERENCES currency (symbol),
    amount          DECIMAL      NOT NULL, -- Use a numeric type for decimal values
    accepted_fee    DECIMAL,
    applied_fee     DECIMAL,
    source_symbol   VARCHAR(255),
    network         VARCHAR(255),
    source_address  VARCHAR(255),
    note            TEXT,
    transaction_ref VARCHAR(255),
    status          VARCHAR(255),
    deposit_type    VARCHAR(255),
    create_date     TIMESTAMP
);


CREATE TABLE IF NOT EXISTS currency_off_chain_gateway
(
    id                    SERIAL PRIMARY KEY,
    currency_symbol       VARCHAR(72) NOT NULL ,
    gateway_uuid             VARCHAR(256) NOT NULL UNIQUE DEFAULT  uuid_generate_v4(),
    withdraw_allowed      BOOLEAN     NOT NULL,
    deposit_allowed      BOOLEAN     NOT NULL,
    withdraw_fee          DECIMAL     NOT NULL,
    withdraw_min          DECIMAL     NOT NULL,
    withdraw_max          DECIMAL     NOT NULL,
    deposit_min          DECIMAL     NOT NULL,
    deposit_max          DECIMAL     NOT NULL,
    is_active             BOOLEAN     NOT NULL DEFAULT TRUE,
    transfer_method      VARCHAR(256) NOT NULL
    );


CREATE TABLE IF NOT EXISTS currency_manual_gateway
(
    id                    SERIAL PRIMARY KEY,
    currency_symbol       VARCHAR(72) NOT NULL ,
    gateway_uuid             VARCHAR(256) NOT NULL UNIQUE DEFAULT  uuid_generate_v4(),
    withdraw_allowed      BOOLEAN     NOT NULL,
    deposit_allowed      BOOLEAN     NOT NULL,
    withdraw_fee          DECIMAL     NOT NULL,
    withdraw_min          DECIMAL     NOT NULL,
    withdraw_max          DECIMAL     NOT NULL,
    deposit_min          DECIMAL     NOT NULL,
    deposit_max          DECIMAL     NOT NULL,
    is_active             BOOLEAN     NOT NULL DEFAULT TRUE,
    allowed_for      VARCHAR(256) NOT NULL
    );