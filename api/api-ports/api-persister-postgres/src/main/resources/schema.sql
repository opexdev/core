CREATE TABLE IF NOT EXISTS symbol_maps
(
    id        SERIAL PRIMARY KEY,
    symbol    VARCHAR(72) NOT NULL,
    alias_key VARCHAR(72) NOT NULL,
    alias     VARCHAR(72) NOT NULL,
    UNIQUE (symbol, alias_key, alias)
);
DROP TABLE IF EXISTS api_key;

CREATE TABLE IF NOT EXISTS api_key_registry
(
    api_key_id         VARCHAR(128) PRIMARY KEY,
    label              VARCHAR(200),
    encrypted_secret   TEXT         NOT NULL,
    enabled            BOOLEAN      NOT NULL DEFAULT TRUE,
    allowed_ips        TEXT,
    allowed_endpoints  TEXT,
    keycloak_user_id   VARCHAR(128),
    keycloak_username  VARCHAR(256),
    created_at         TIMESTAMP    NOT NULL,
    updated_at         TIMESTAMP    NOT NULL
);
