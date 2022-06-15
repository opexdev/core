CREATE TABLE IF NOT EXISTS symbol_maps
(
    id          SERIAL PRIMARY KEY,
    symbol      VARCHAR(72) NOT NULL,
    alias_key    VARCHAR(72) NOT NULL,
    alias       VARCHAR(72) NOT NULL,
    UNIQUE (symbol, alias_key, alias)
);
