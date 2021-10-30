CREATE TABLE IF NOT EXISTS orders
  (
     id                     SERIAL PRIMARY KEY,
     ouid                   VARCHAR(72) NOT NULL UNIQUE,
     uuid                   VARCHAR(72) NOT NULL,
     client_order_id        VARCHAR(72),
     symbol                 VARCHAR(20),
     order_id               NUMERIC,
     maker_fee              DECIMAL,
     taker_fee              DECIMAL,
     left_side_fraction     DECIMAL,
     right_side_fraction    DECIMAL,
     user_level             VARCHAR(20),
     side                   VARCHAR(20),
     match_constraint       VARCHAR(20),
     order_type             VARCHAR(20),
     price                  DECIMAL,
     quantity               DECIMAL,
     quote_quantity         DECIMAL,
     executed_qty           DECIMAL,
     accumulative_quote_qty DECIMAL,
     status                 INTEGER,
     create_date            TIMESTAMP,
     update_date            TIMESTAMP,
     version                NUMERIC
  );

CREATE TABLE IF NOT EXISTS trades
  (
     id                    SERIAL PRIMARY KEY,
     trade_id              NUMERIC,
     symbol                VARCHAR(20),
     matched_quantity      DECIMAL,
     taker_price           DECIMAL,
     maker_price           DECIMAL,
     taker_commision       DECIMAL,
     maker_commision       DECIMAL,
     taker_commision_asset VARCHAR(20),
     maker_commision_asset VARCHAR(20),
     trade_date            TIMESTAMP,
     maker_ouid            VARCHAR(72) NOT NULL,
     taker_ouid            VARCHAR(72) NOT NULL,
     maker_uuid            VARCHAR(72) NOT NULL,
     taker_uuid            VARCHAR(72) NOT NULL,
     create_date           TIMESTAMP
  );

CREATE TABLE IF NOT EXISTS symbol_maps
  (
     symbol VARCHAR(72) PRIMARY KEY,
     value  VARCHAR(72) UNIQUE NOT NULL
  );

CREATE OR REPLACE FUNCTION interval_generator(start_ts TIMESTAMP without TIME ZONE, end_ts TIMESTAMP without TIME ZONE, round_interval INTERVAL)
    RETURNS TABLE(start_time TIMESTAMP without TIME ZONE, end_time TIMESTAMP without TIME ZONE) as $$
BEGIN
    RETURN QUERY SELECT (n) start_time, (n + round_interval) end_time
        FROM generate_series(date_trunc('minute', start_ts), end_ts, round_interval) n;
END;
$$ LANGUAGE 'plpgsql';
