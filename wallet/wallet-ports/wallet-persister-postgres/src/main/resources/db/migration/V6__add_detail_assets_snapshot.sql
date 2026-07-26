CREATE TABLE IF NOT EXISTS detail_assets_snapshot
(
    id         SERIAL PRIMARY KEY,
    uuid    VARCHAR(36) NOT NULL,
    currency   VARCHAR(50) NOT NULL REFERENCES currency (symbol),
    volume     DECIMAL     NOT NULL,
    total_amount DECIMAL     NOT NULL,
    quote_currency       VARCHAR(50) NOT NULL REFERENCES currency (symbol),
    snapshot_date  TIMESTAMP NOT NULL,
    batch_number INTEGER NOT NULL ,
    unique (uuid, currency, snapshot_date , quote_currency)
);