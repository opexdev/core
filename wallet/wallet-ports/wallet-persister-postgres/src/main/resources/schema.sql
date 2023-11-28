CREATE TABLE IF NOT EXISTS currency
(
    symbol    VARCHAR(25)  PRIMARY KEY,
    name      VARCHAR(25),
    precision DECIMAL NOT NULL,
    title VARCHAR(25),
    alias VARCHAR(25),
    max_deposit DECIMAL,
    min_deposit DECIMAL,
    min_Withdraw DECIMAL,
    max_withdraw DECIMAL,
    icon TEXT,
    last_update_date TIMESTAMP,
    create_date TIMESTAMP
);

ALTER TABLE currency ADD COLUMN IF NOT EXISTS title  VARCHAR(25);
ALTER TABLE currency ADD COLUMN IF NOT EXISTS alias  VARCHAR(25);
ALTER TABLE currency ADD COLUMN IF NOT EXISTS max_deposit  DECIMAL;
ALTER TABLE currency ADD COLUMN IF NOT EXISTS min_deposit  DECIMAL;
ALTER TABLE currency ADD COLUMN IF NOT EXISTS min_Withdraw  DECIMAL;
ALTER TABLE currency ADD COLUMN IF NOT EXISTS max_withdraw  DECIMAL;
ALTER TABLE currency ADD COLUMN IF NOT EXISTS icon  TEXT;
ALTER TABLE currency ADD COLUMN IF NOT EXISTS last_update_date TIMESTAMP;
ALTER TABLE currency ADD COLUMN IF NOT EXISTS create_date TIMESTAMP;





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

ALTER TABLE wallet ADD COLUMN IF NOT EXISTS version INTEGER;

CREATE TABLE IF NOT EXISTS transaction
(
    id               SERIAL PRIMARY KEY,
    source_wallet    INTEGER   NOT NULL REFERENCES wallet (id),
    dest_wallet      INTEGER   NOT NULL REFERENCES wallet (id),
    source_amount    DECIMAL   NOT NULL,
    dest_amount      DECIMAL   NOT NULL,
    description      TEXT,
    transfer_ref     TEXT UNIQUE,
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_DATE
);

ALTER TABLE transaction ADD COLUMN IF NOT EXISTS transfer_detail_json TEXT;
ALTER TABLE transaction ADD COLUMN IF NOT EXISTS transfer_category VARCHAR(36);

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
    wallet               INTEGER NOT NULL REFERENCES wallet (id),
    amount               DECIMAL NOT NULL,
    accepted_fee         DECIMAL NOT NULL,
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
    create_date          TIMESTAMP NOT NULL,
    accept_date          TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rate
(
    id SERIAL PRIMARY KEY,
    source_currency VARCHAR(25) NOT NULL REFERENCES currency (symbol),
    destination_currency VARCHAR(25) NOT NULL REFERENCES currency (symbol),
    rate         DECIMAL ,
    is_forbidden BOOLEAN,
    last_update_date TIMESTAMP,
    create_date TIMESTAMP
    );
ALTER TABLE rate ADD COLUMN IF NOT EXISTS create_date TIMESTAMP;


CREATE TABLE IF NOT EXISTS forbidden_rate
(
    id SERIAL PRIMARY KEY,
    source_currency VARCHAR(25) NOT NULL REFERENCES currency (symbol),
    destination_currency VARCHAR(25) NOT NULL REFERENCES currency (symbol),
    last_update_date TIMESTAMP,
    create_date TIMESTAMP
    );
