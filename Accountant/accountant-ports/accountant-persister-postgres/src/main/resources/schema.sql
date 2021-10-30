CREATE TABLE IF NOT EXISTS orders
  (
     id                       SERIAL PRIMARY KEY,
     ouid                     VARCHAR(72) NOT NULL UNIQUE,
     uuid                     VARCHAR(72) NOT NULL,
     pair                     VARCHAR(20),
     matching_engine_id       NUMERIC,
     maker_fee                DECIMAL,
     taker_fee                DECIMAL,
     left_side_fraction       DECIMAL,
     right_side_fraction      DECIMAL,
     user_level               VARCHAR(20),
     direction                VARCHAR(20),
     match_constraint         VARCHAR(30),
     order_type               VARCHAR(30),
     price                    NUMERIC,
     quantity                 NUMERIC,
     filled_quantity          NUMERIC,
     orig_price               DECIMAL,
     orig_quantity            DECIMAL,
     filled_orig_quantity     DECIMAL,
     first_transfer_amount    NUMERIC,
     remained_transfer_amount NUMERIC,
     status                   INTEGER,
     agent                    VARCHAR(20),
     ip                       VARCHAR(11),
     create_date              TIMESTAMP
  );

CREATE TABLE IF NOT EXISTS fi_actions
  (
     id                   SERIAL PRIMARY KEY,
     parent_id            NUMERIC,
     event_type           VARCHAR(72) NOT NULL,
     pointer              VARCHAR(72) NOT NULL,
     symbol               VARCHAR(36),
     amount               DECIMAL,
     sender               VARCHAR(36),
     sender_wallet_type   VARCHAR(36),
     receiver             VARCHAR(36),
     receiver_wallet_type VARCHAR(36),
     agent                VARCHAR(20),
     ip                   VARCHAR(11),
     create_date          TIMESTAMP,
     status               VARCHAR(20),
     retry_count          NUMERIC,
     last_try_date        TIMESTAMP
  );

CREATE TABLE IF NOT EXISTS pair_config
  (
     pair                     VARCHAR(72) PRIMARY KEY,
     left_side_wallet_symbol  VARCHAR(36) NOT NULL,
     right_side_wallet_symbol VARCHAR(36) NOT NULL,
     left_side_fraction       DECIMAL,
     right_side_fraction      DECIMAL,
     rate                     DECIMAL
  );

CREATE TABLE IF NOT EXISTS pair_fee_config
  (
     id             SERIAL PRIMARY KEY,
     pair_config_id VARCHAR(72),
     direction      VARCHAR(36) NOT NULL,
     user_level     VARCHAR(36) NOT NULL,
     maker_fee      DECIMAL,
     taker_fee      DECIMAL
  );

CREATE TABLE IF NOT EXISTS temp_events
  (
     id         SERIAL PRIMARY KEY,
     ouid       VARCHAR(72) NOT NULL,
     event_type VARCHAR(72) NOT NULL,
     event_body TEXT,
     event_date TIMESTAMP
  );

COMMIT;
