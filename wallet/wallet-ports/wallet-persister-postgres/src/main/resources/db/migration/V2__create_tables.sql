CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS currency
(
    symbol            VARCHAR(25) PRIMARY KEY,
    name              VARCHAR(25),
    precision         DECIMAL      NOT NULL,
    title             VARCHAR(25),
    alias             VARCHAR(25),
    icon              TEXT,
    last_update_date  TIMESTAMP,
    create_date       TIMESTAMP,
    is_transitive     BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active         BOOLEAN      NOT NULL DEFAULT TRUE,
    sign              VARCHAR(25),
    description       TEXT,
    short_description TEXT,
    uuid              VARCHAR(256) NOT NULL DEFAULT uuid_generate_v4(),
    external_url      VARCHAR(255),
    display_order     INTEGER,
    max_order         DECIMAL
);



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
    version     INTEGER,
    UNIQUE (owner, wallet_type, currency)
);



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
    last_update_date     TIMESTAMP,
    applicator           VARCHAR(80),
    withdraw_type        VARCHAR(255),
    attachment           VARCHAR(255),
    transfer_method VARCHAR(255)
);

ALTER TABLE withdraws
    add COLUMN IF NOT EXISTS attachment VARCHAR(255);

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
    transaction_ref VARCHAR(255) UNIQUE,
    status          VARCHAR(255),
    deposit_type    VARCHAR(255),
    create_date     TIMESTAMP,
    attachment      VARCHAR(255),
    transfer_method VARCHAR(255)
);
ALTER TABLE deposits
    add COLUMN IF NOT EXISTS attachment VARCHAR(255);

CREATE TABLE IF NOT EXISTS currency_off_chain_gateway
(
    id               SERIAL PRIMARY KEY,
    currency_symbol  VARCHAR(72)  NOT NULL,
    gateway_uuid     VARCHAR(256) NOT NULL UNIQUE DEFAULT uuid_generate_v4(),
    withdraw_allowed BOOLEAN      NOT NULL,
    deposit_allowed  BOOLEAN      NOT NULL,
    withdraw_fee     DECIMAL      NOT NULL,
    withdraw_min     DECIMAL      NOT NULL,
    withdraw_max     DECIMAL      NOT NULL,
    deposit_min      DECIMAL      NOT NULL,
    deposit_max      DECIMAL      NOT NULL,
    is_deposit_active        BOOLEAN      NOT NULL        DEFAULT TRUE,
    is_withdraw_active        BOOLEAN      NOT NULL        DEFAULT TRUE,
    transfer_method  VARCHAR(256) NOT NULL,
    deposit_description            TEXT,
    withdraw_description            TEXT,
    display_order     INTEGER,
    UNIQUE (currency_symbol, transfer_method)

);

ALTER TABLE currency_off_chain_gateway
    add COLUMN IF NOT EXISTS deposit_description TEXT;


ALTER TABLE currency_off_chain_gateway
    add COLUMN IF NOT EXISTS withdraw_description TEXT;

ALTER TABLE currency_off_chain_gateway
    drop COLUMN  IF EXISTS description;


-- CREATE TABLE IF NOT EXISTS currency_manual_gateway
-- (
--     id               SERIAL PRIMARY KEY,
--     currency_symbol  VARCHAR(72)  NOT NULL,
--     gateway_uuid     VARCHAR(256) NOT NULL UNIQUE DEFAULT uuid_generate_v4(),
--     withdraw_allowed BOOLEAN      NOT NULL,
--     deposit_allowed  BOOLEAN      NOT NULL,
--     withdraw_fee     DECIMAL      NOT NULL,
--     withdraw_min     DECIMAL      NOT NULL,
--     withdraw_max     DECIMAL      NOT NULL,
--     deposit_min      DECIMAL      NOT NULL,
--     deposit_max      DECIMAL      NOT NULL,
--     is_active        BOOLEAN      NOT NULL        DEFAULT TRUE,
--     allowed_for      VARCHAR(256) NOT NULL,
--     UNIQUE (currency_symbol, allowed_for)
--
-- );

CREATE TABLE IF NOT EXISTS terminal
(
    id              SERIAL PRIMARY KEY,
    uuid            VARCHAR(256) NOT NULL,
    owner           VARCHAR(255) NOT NULL,
    identifier      VARCHAR(255) NOT NULL,
    active          BOOLEAN DEFAULT TRUE,
    type            VARCHAR(255) NOT NULL,
    bank_swift_code VARCHAR(255) NOT NULL,
    display_order     INTEGER
);

CREATE TABLE IF NOT EXISTS gateway_terminal
(
    id          SERIAL PRIMARY KEY,
    terminal_id BIGINT NOT NULL REFERENCES terminal (id) ON DELETE CASCADE,
    gateway_id  BIGINT NOT NULL REFERENCES currency_off_chain_gateway (id) ON DELETE CASCADE,
    UNIQUE (terminal_id, gateway_id)

);

---------------------------------------------------------------------------
------------------------ Withdraw from otc to opex ------------------------
---------------------------------------------------------------------------
UPDATE currency_off_chain_gateway
SET transfer_method = CASE
                          WHEN transfer_method = 'Card2card' THEN 'CARD'
                          WHEN transfer_method = 'Sheba' THEN 'SHEBA' END
WHERE transfer_method IN ('Card2card', 'Sheba');



UPDATE withdraws
SET dest_network = CASE WHEN dest_network = 'Card2card' THEN 'CARD' WHEN dest_network = 'Sheba' THEN 'SHEBA' END
WHERE dest_network IN ('Card2card', 'Sheba');
CREATE TABLE IF NOT EXISTS voucher_group
(
    id          SERIAL PRIMARY KEY,
    issuer      VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS voucher
(
    id           SERIAL PRIMARY KEY,
    private_code VARCHAR(255) NOT NULL UNIQUE,
    public_code  VARCHAR(255) NOT NULL UNIQUE,
    amount       DECIMAL      NOT NULL,
    currency     VARCHAR(25)  NOT NULL REFERENCES currency (symbol),
    status       VARCHAR(20)  NOT NULL,
    expire_date  TIMESTAMP    NOT NULL,
    create_date  TIMESTAMP    NOT NULL,
    use_date     TIMESTAMP,
    uuid         VARCHAR(36),
    voucher_group     INTEGER REFERENCES voucher_group (id) NOT NULL
);

DROP TABLE IF EXISTS currency_manual_gateway;

CREATE TABLE IF NOT EXISTS voucher_usage
(
    id           SERIAL PRIMARY KEY,
    voucher      INTEGER  NOT NULL REFERENCES voucher (id),
    use_date     TIMESTAMP,
    uuid         VARCHAR(36),
    type VARCHAR(20),
    status VARCHAR(20),
    remaining_usage INTEGER,
    version INTEGER DEFAULT 1,
    user_limit INTEGER
);
CREATE INDEX IF NOT EXISTS idx_voucher_usage_voucher ON voucher_usage (voucher);
CREATE INDEX IF NOT EXISTS idx_voucher_usage_uuid ON voucher_usage (uuid);

CREATE TABLE IF NOT EXISTS voucher_sale_data
(
    id                    SERIAL PRIMARY KEY,
    voucher               INTEGER  NOT NULL UNIQUE REFERENCES voucher (id),
    national_code         VARCHAR(10) NOT NULL ,
    phone_number          VARCHAR(11) NOT NULL ,
    transaction_number    VARCHAR(255) NOT NULL UNIQUE ,
    transaction_amount    DECIMAL NOT NULL ,
    sale_date             TIMESTAMP NOT NULL ,
    seller_uuid           VARCHAR(36) NOT NULL
);

CREATE TABLE IF NOT EXISTS quote_currency
(
    id          SERIAL PRIMARY KEY,
    currency    VARCHAR(25) NOT NULL UNIQUE REFERENCES currency (symbol),
    is_active         BOOLEAN      NOT NULL DEFAULT false,
    last_update_date TIMESTAMP
);

