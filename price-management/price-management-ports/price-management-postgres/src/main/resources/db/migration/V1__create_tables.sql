CREATE TABLE pair_rate_config
(
    id         SERIAL PRIMARY KEY,
    symbol     VARCHAR(25) NOT NULL UNIQUE,
    -- Required for AUTO, null for MANUAL (a manual price doesn't get aggregated/marked up).
    strategy   VARCHAR(50),
    margin     DECIMAL,
    is_active  BOOLEAN     NOT NULL DEFAULT TRUE,
    -- AUTO: scheduler aggregates from providers. MANUAL: scheduler skips this symbol entirely;
    -- price only changes when an admin submits one (see rate_history.source below).
    price_mode VARCHAR(10) NOT NULL DEFAULT 'AUTO'
);
-- Providers an admin has explicitly selected to be used for a symbol (a whitelist), submitted
-- together with the symbol's pair_rate_config. Replaces the old exclude-list design.
CREATE TABLE pair_provider_include
(
    id       SERIAL PRIMARY KEY,
    symbol   VARCHAR(25) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    CONSTRAINT uk_pair_provider_include UNIQUE (symbol, provider)
);
CREATE TABLE rate_history
(
    id           BIGSERIAL PRIMARY KEY,
    symbol       VARCHAR(25) NOT NULL,
    price        DECIMAL     NOT NULL,
    created_date TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Whether this row came from the AUTO scheduler or was pushed by an admin (MANUAL).
    source       VARCHAR(10) NOT NULL DEFAULT 'AUTO'
);

CREATE INDEX idx_rate_history_symbol_created_date ON rate_history (symbol, created_date DESC);
CREATE INDEX idx_pair_rate_config_active ON pair_rate_config (is_active) WHERE is_active = true;

