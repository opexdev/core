CREATE TABLE IF NOT EXISTS opex_orders
(
    id
    SERIAL
    PRIMARY
    KEY,
    ouid
    VARCHAR
(
    72
) NOT NULL UNIQUE,
    symbol VARCHAR
(
    20
) NOT NULL,
    direction VARCHAR
(
    20
) NOT NULL,
    match_constraint VARCHAR
(
    20
) NOT NULL,
    order_type VARCHAR
(
    20
) NOT NULL,
    uuid VARCHAR
(
    72
) NOT NULL,
    agent VARCHAR
(
    20
),
    ip VARCHAR
(
    11
),
    order_date TIMESTAMP NOT NULL,
    create_date TIMESTAMP NOT NULL
    );

CREATE TABLE IF NOT EXISTS opex_order_events
(
    id
    SERIAL
    PRIMARY
    KEY,
    ouid
    VARCHAR
(
    72
) NOT NULL,
    matching_orderid BIGINT,
    price BIGINT,
    quantity BIGINT,
    filled_quantity BIGINT,
    uuid VARCHAR
(
    72
) NOT NULL,
    event VARCHAR
(
    30
) NOT NULL,
    agent VARCHAR
(
    20
),
    ip VARCHAR
(
    11
),
    event_date TIMESTAMP NOT NULL,
    create_date TIMESTAMP NOT NULL
    );

CREATE TABLE IF NOT EXISTS opex_events
(
    id
    SERIAL
    PRIMARY
    KEY,
    correlation_id
    VARCHAR
(
    72
) NOT NULL,
    ouid VARCHAR
(
    72
) NOT NULL,
    uuid VARCHAR
(
    72
) NOT NULL,
    symbol VARCHAR
(
    20
) NOT NULL,
    event VARCHAR
(
    30
) NOT NULL,
    event_json TEXT NOT NULL,
    agent VARCHAR
(
    20
),
    ip VARCHAR
(
    11
),
    event_date TIMESTAMP NOT NULL,
    create_date TIMESTAMP NOT NULL
    );

CREATE TABLE IF NOT EXISTS dead_letter_events
(
    id
    SERIAL
    PRIMARY
    KEY,
    origin_module
    VARCHAR
(
    72
) NOT NULL,
    origin_topic VARCHAR
(
    72
),
    consumer_group VARCHAR
(
    72
),
    exception_message TEXT,
    exception_stacktrace TEXT,
    exception_class_name TEXT,
    timestamp TIMESTAMP NOT NULL,
    value TEXT
    )

;

CREATE TABLE IF NOT EXISTS opex_published_events
(
    id              SERIAL PRIMARY KEY,
    topic           VARCHAR(200) NOT NULL,
    partition_id    INTEGER      NOT NULL,
    event_offset    BIGINT       NOT NULL,
    event_key       TEXT,
    event_payload   TEXT         NOT NULL,
    event_timestamp TIMESTAMP    NOT NULL,
    create_date     TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_opex_published_events_topic_partition_offset
    ON opex_published_events (topic, partition_id, event_offset);

CREATE TABLE IF NOT EXISTS opex_accounting_transactions
(
    id           SERIAL PRIMARY KEY,
    tx_id        VARCHAR(200)   NOT NULL,
    event_id     VARCHAR(200),
    user_id      VARCHAR(72)    NOT NULL,
    type         VARCHAR(30)    NOT NULL,
    symbol       VARCHAR(30),
    asset        VARCHAR(20)    NOT NULL,
    amount       NUMERIC(30, 8) NOT NULL,
    fee          NUMERIC(30, 8),
    fee_asset    VARCHAR(20),
    principal_amount NUMERIC(30, 8),
    gross_amount NUMERIC(30, 8),
    withdraw_status VARCHAR(30),
    transfer_category VARCHAR(50),
    operation_id VARCHAR(200),
    business_ref VARCHAR(255),
    wallet_tx_ref VARCHAR(200),
    record_role  VARCHAR(50),
    source       VARCHAR(100)   NOT NULL,
    reference_id VARCHAR(200),
    side         VARCHAR(20),
    price        NUMERIC(30, 8),
    quantity     NUMERIC(30, 8),
    operation_created_at TIMESTAMP,
    lifecycle_transition_at TIMESTAMP,
    completed_at TIMESTAMP,
    occurred_at  TIMESTAMP      NOT NULL,
    create_date  TIMESTAMP      NOT NULL
);

-- Additive upgrade: preserve legacy records; unknown parties remain NULL.
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS event_id VARCHAR(200);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS fee_asset VARCHAR(20);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS principal_amount NUMERIC(30,8);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS gross_amount NUMERIC(30,8);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS withdraw_status VARCHAR(30);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS transfer_category VARCHAR(50);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS operation_id VARCHAR(200);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS business_ref VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS wallet_tx_ref VARCHAR(200);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS record_role VARCHAR(50);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS operation_created_at TIMESTAMP;
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS lifecycle_transition_at TIMESTAMP;
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP;
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS sender_party_id VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS receiver_party_id VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS sender_wallet_id VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS receiver_wallet_id VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS sender_wallet_type VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS receiver_wallet_type VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS perspective_party_id VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS counterparty_reference_id VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS related_transaction_id VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS source_asset VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS source_amount NUMERIC(38,18);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS destination_asset VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS destination_amount NUMERIC(38,18);
ALTER TABLE opex_accounting_transactions ALTER COLUMN fee DROP NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uidx_opex_accounting_transactions_txid_source
    ON opex_accounting_transactions (tx_id, source);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_opex_accounting_transactions_eventid_source
    ON opex_accounting_transactions (event_id, source);

CREATE INDEX IF NOT EXISTS idx_opex_accounting_transactions_occurred_at
    ON opex_accounting_transactions (occurred_at DESC);

-- Trade capture metadata. Matching-engine trade numbers are informational, not unique.
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS original_trade_id VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS base_asset VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS quote_asset VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS quote_amount NUMERIC;
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS trade_side VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS participant_role VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS order_reference VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS counterparty_order_reference VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS financial_action_id VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS related_financial_action_id VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS fee_action_id VARCHAR(255);
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS expected_fee NUMERIC;
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS expected_fee_asset VARCHAR(255);
ALTER TABLE opex_accounting_transactions ALTER COLUMN amount TYPE NUMERIC;
ALTER TABLE opex_accounting_transactions ALTER COLUMN fee TYPE NUMERIC;
ALTER TABLE opex_accounting_transactions ALTER COLUMN price TYPE NUMERIC;
ALTER TABLE opex_accounting_transactions ALTER COLUMN quantity TYPE NUMERIC;
ALTER TABLE opex_accounting_transactions ALTER COLUMN principal_amount TYPE NUMERIC;
ALTER TABLE opex_accounting_transactions ALTER COLUMN gross_amount TYPE NUMERIC;
ALTER TABLE opex_accounting_transactions ALTER COLUMN source_amount TYPE NUMERIC;
ALTER TABLE opex_accounting_transactions ALTER COLUMN destination_amount TYPE NUMERIC;
CREATE INDEX IF NOT EXISTS idx_accounting_financial_action ON opex_accounting_transactions (financial_action_id);
CREATE INDEX IF NOT EXISTS idx_accounting_fee_action ON opex_accounting_transactions (fee_action_id);

-- Authoritative enriched execution source. Existing legacy tables are not dropped.
CREATE TABLE IF NOT EXISTS opex_rich_trades (
    id BIGSERIAL PRIMARY KEY,
    execution_reference TEXT NOT NULL,
    original_trade_id BIGINT NOT NULL,
    pair TEXT NOT NULL,
    taker_ouid TEXT NOT NULL,
    taker_uuid TEXT NOT NULL,
    taker_order_id BIGINT NOT NULL,
    taker_direction TEXT NOT NULL,
    taker_price NUMERIC NOT NULL,
    taker_quantity NUMERIC NOT NULL,
    taker_quote_quantity NUMERIC NOT NULL,
    taker_remained_quantity NUMERIC NOT NULL,
    taker_commision NUMERIC NOT NULL,
    taker_commision_asset TEXT NOT NULL,
    maker_ouid TEXT NOT NULL,
    maker_uuid TEXT NOT NULL,
    maker_order_id BIGINT NOT NULL,
    maker_direction TEXT NOT NULL,
    maker_price NUMERIC NOT NULL,
    maker_quantity NUMERIC NOT NULL,
    maker_quote_quantity NUMERIC NOT NULL,
    maker_remained_quantity NUMERIC NOT NULL,
    maker_commision NUMERIC NOT NULL,
    maker_commision_asset TEXT NOT NULL,
    matched_price NUMERIC NOT NULL,
    matched_quantity NUMERIC NOT NULL,
    trade_date_time TIMESTAMP NOT NULL,
    base_asset TEXT NOT NULL,
    quote_asset TEXT NOT NULL,
    taker_settlement_action_id TEXT,
    maker_settlement_action_id TEXT,
    taker_fee_action_id TEXT,
    maker_fee_action_id TEXT,
    source_topic TEXT NOT NULL,
    source_partition INTEGER NOT NULL,
    source_offset BIGINT NOT NULL,
    captured_at TIMESTAMP NOT NULL,
    UNIQUE (execution_reference),
    UNIQUE (source_topic, source_partition, source_offset)
);
CREATE INDEX IF NOT EXISTS idx_rich_trades_date ON opex_rich_trades (trade_date_time DESC);

-- Authoritative complete OTC operation; leg views are derived on read.
CREATE TABLE IF NOT EXISTS opex_swaps (
    id BIGSERIAL PRIMARY KEY,
    uuid TEXT NOT NULL,
    tx_id TEXT NOT NULL,
    source_asset TEXT NOT NULL,
    source_amount NUMERIC NOT NULL,
    dest_asset TEXT NOT NULL,
    dest_amount NUMERIC NOT NULL,
    source_transaction_id TEXT NOT NULL,
    destination_transaction_id TEXT NOT NULL,
    source_reference TEXT,
    destination_reference TEXT,
    system_party_id TEXT,
    receiver_party_id TEXT,
    source_sender_wallet_id TEXT,
    source_receiver_wallet_id TEXT,
    destination_sender_wallet_id TEXT,
    destination_receiver_wallet_id TEXT,
    source_sender_wallet_type TEXT,
    source_receiver_wallet_type TEXT,
    destination_sender_wallet_type TEXT,
    destination_receiver_wallet_type TEXT,
    reservation_id BIGINT,
    reservation_reference TEXT,
    quoted_rate NUMERIC,
    quoted_at TIMESTAMP,
    quote_expires_at TIMESTAMP,
    transfer_category TEXT,
    reserved_by TEXT,
    executed_by TEXT,
    description TEXT,
    status TEXT,
    create_date TIMESTAMP NOT NULL,
    source_topic TEXT NOT NULL,
    source_partition INTEGER NOT NULL,
    source_offset BIGINT NOT NULL,
    captured_at TIMESTAMP NOT NULL,
    UNIQUE (tx_id),
    UNIQUE (source_topic, source_partition, source_offset)
);
CREATE INDEX IF NOT EXISTS idx_swaps_date ON opex_swaps (create_date DESC);

ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS transfer_method TEXT;
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS applicator TEXT;
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS deposit_status TEXT;
ALTER TABLE opex_accounting_transactions ADD COLUMN IF NOT EXISTS operation_type TEXT;

-- Complete funding lifecycle snapshots; no overwrite of previous statuses.
CREATE TABLE IF NOT EXISTS opex_funding_operations (
    id BIGSERIAL PRIMARY KEY,
    event_id TEXT NOT NULL,
    operation_id TEXT NOT NULL,
    type TEXT NOT NULL,
    user_id TEXT NOT NULL,
    operation_database_id BIGINT,
    currency TEXT NOT NULL,
    amount NUMERIC NOT NULL,
    principal_amount NUMERIC,
    applied_customer_fee NUMERIC,
    fee_asset TEXT,
    accepted_fee NUMERIC,
    status TEXT NOT NULL,
    transfer_category TEXT,
    transfer_method TEXT,
    operation_type TEXT,
    applicator TEXT,
    gateway_id TEXT,
    network TEXT,
    business_reference TEXT,
    counterparty_reference_id TEXT,
    wallet_transaction_ref TEXT,
    request_transaction_id TEXT,
    finalized_transaction_id TEXT,
    operation_wallet_id TEXT,
    sender_party_id TEXT,
    receiver_party_id TEXT,
    sender_wallet_id TEXT,
    receiver_wallet_id TEXT,
    sender_wallet_type TEXT,
    receiver_wallet_type TEXT,
    source_symbol TEXT,
    source_address TEXT,
    destination_address TEXT,
    destination_asset TEXT,
    destination_amount NUMERIC,
    note TEXT,
    status_reason TEXT,
    attachment TEXT,
    otp_required INTEGER,
    operation_created_at TIMESTAMP,
    lifecycle_transition_at TIMESTAMP,
    completed_at TIMESTAMP,
    occurred_at TIMESTAMP NOT NULL,
    source_topic TEXT NOT NULL,
    source_partition INTEGER NOT NULL,
    source_offset BIGINT NOT NULL,
    captured_at TIMESTAMP NOT NULL,
    UNIQUE (type, event_id),
    UNIQUE (source_topic, source_partition, source_offset)
);
CREATE INDEX IF NOT EXISTS idx_funding_operation_history ON opex_funding_operations (type, operation_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_funding_occurred ON opex_funding_operations (occurred_at DESC);
