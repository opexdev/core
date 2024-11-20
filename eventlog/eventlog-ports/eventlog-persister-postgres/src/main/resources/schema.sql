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

CREATE TABLE IF NOT EXISTS opex_trades
(
    id
    SERIAL
    PRIMARY
    KEY,
    symbol
    VARCHAR
(
    20
) NOT NULL,
    taker_ouid VARCHAR
(
    72
) NOT NULL,
    taker_uuid VARCHAR
(
    72
) NOT NULL,
    taker_matching_orderid BIGINT NOT NULL,
    taker_direction VARCHAR
(
    20
) NOT NULL,
    taker_price BIGINT NOT NULL,
    taker_remained_quantity BIGINT NOT NULL,
    maker_ouid VARCHAR
(
    72
) NOT NULL,
    maker_uuid VARCHAR
(
    72
) NOT NULL,
    maker_matching_orderid BIGINT NOT NULL,
    maker_direction VARCHAR
(
    20
) NOT NULL,
    maker_price BIGINT NOT NULL,
    maker_remained_quantity BIGINT NOT NULL,
    matched_quantity BIGINT NOT NULL,
    trade_date TIMESTAMP NOT NULL,
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
