CREATE TABLE pair_rate_config
(
    id         SERIAL PRIMARY KEY,
    symbol     VARCHAR(25) NOT NULL UNIQUE,
    strategy   VARCHAR(50),
    margin     DECIMAL,
    is_active  BOOLEAN     NOT NULL DEFAULT TRUE,
    price_mode VARCHAR(10) NOT NULL DEFAULT 'AUTO'
);
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
    source       VARCHAR(10) NOT NULL DEFAULT 'AUTO'
);

CREATE INDEX idx_rate_history_symbol_created_date ON rate_history (symbol, created_date DESC);
CREATE INDEX idx_pair_rate_config_active ON pair_rate_config (is_active) WHERE is_active = true;

