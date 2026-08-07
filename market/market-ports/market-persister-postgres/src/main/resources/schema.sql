CREATE TABLE IF NOT EXISTS orders
(
    id                  SERIAL PRIMARY KEY,
    ouid                VARCHAR(72) NOT NULL UNIQUE,
    uuid                VARCHAR(72) NOT NULL,
    client_order_id     VARCHAR(72),
    symbol              VARCHAR(20) NOT NULL,
    order_id            INTEGER,
    maker_fee           DECIMAL,
    taker_fee           DECIMAL,
    left_side_fraction  DECIMAL,
    right_side_fraction DECIMAL,
    user_level          VARCHAR(20),
    side                VARCHAR(20),
    match_constraint    VARCHAR(20),
    order_type          VARCHAR(20),
    price               DECIMAL,
    quantity            DECIMAL,
    quote_quantity      DECIMAL,
    create_date         TIMESTAMP,
    update_date         TIMESTAMP   NOT NULL,
    version             INTEGER
);

CREATE TABLE IF NOT EXISTS order_status
(
    id                     SERIAL PRIMARY KEY,
    ouid                   VARCHAR(72) NOT NULL,
    executed_quantity      DECIMAL,
    accumulative_quote_qty DECIMAL,
    status                 INTEGER     NOT NULL,
    appearance             INTEGER     NOT NULL,
    date                   TIMESTAMP   NOT NULL,
    UNIQUE (ouid, status, appearance, executed_quantity)
);
CREATE INDEX IF NOT EXISTS idx_order_status_ouid_rank
    ON order_status (ouid, appearance DESC, executed_quantity DESC, date DESC, id DESC);

CREATE TABLE IF NOT EXISTS open_orders
(
    id                SERIAL PRIMARY KEY,
    ouid              VARCHAR(72) NOT NULL UNIQUE,
    executed_quantity DECIMAL,
    status            INTEGER     NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_orders_uuid_create_date ON orders (uuid, create_date DESC);
CREATE INDEX IF NOT EXISTS idx_orders_uuid_symbol_create_date ON orders (uuid, symbol, create_date DESC);

CREATE TABLE IF NOT EXISTS trades
(
    id                     SERIAL PRIMARY KEY,
    trade_id               INTEGER     NOT NULL,
    symbol                 VARCHAR(20) NOT NULL,
    base_asset             VARCHAR(20) NOT NULL,
    quote_asset            VARCHAR(20) NOT NULL,
    matched_price          DECIMAL     NOT NULL,
    matched_quantity       DECIMAL     NOT NULL,
    taker_price            DECIMAL     NOT NULL,
    maker_price            DECIMAL     NOT NULL,
    taker_commission       DECIMAL,
    maker_commission       DECIMAL,
    taker_commission_asset VARCHAR(20),
    maker_commission_asset VARCHAR(20),
    trade_date             TIMESTAMP   NOT NULL,
    maker_ouid             VARCHAR(72) NOT NULL,
    taker_ouid             VARCHAR(72) NOT NULL,
    maker_uuid             VARCHAR(72) NOT NULL,
    taker_uuid             VARCHAR(72) NOT NULL,
    create_date            TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_trades_symbol on trades (symbol);
CREATE INDEX IF NOT EXISTS idx_trades_create_date on trades (create_date);
WITH duplicate_trades AS (
    SELECT id
    FROM (
             SELECT id,
                    ROW_NUMBER() OVER (PARTITION BY symbol, trade_id ORDER BY id) AS rn
             FROM trades
         ) ranked
    WHERE rn > 1
)
DELETE
FROM trades t
    USING duplicate_trades d
WHERE t.id = d.id;
CREATE UNIQUE INDEX IF NOT EXISTS uq_trades_symbol_trade_id on trades (symbol, trade_id);

CREATE OR REPLACE FUNCTION interval_generator(
    start_ts TIMESTAMP without TIME ZONE,
    end_ts TIMESTAMP without TIME ZONE,
    round_interval INTERVAL
)
    RETURNS TABLE
            (
                start_time TIMESTAMP without TIME ZONE,
                end_time   TIMESTAMP without TIME ZONE
            )
as
$$
BEGIN
    RETURN QUERY SELECT (n) start_time, (n + round_interval) end_time
                 FROM generate_series(date_trunc('minute', start_ts), end_ts, round_interval) n;
END;

$$ LANGUAGE 'plpgsql';
