CREATE TABLE IF NOT EXISTS symbol_maps
(
    id        SERIAL PRIMARY KEY,
    symbol    VARCHAR(72) NOT NULL,
    alias_key VARCHAR(72) NOT NULL,
    alias     VARCHAR(72) NOT NULL,
    UNIQUE (symbol, alias_key, alias)
);

CREATE TABLE IF NOT EXISTS api_key
(
    id                    SERIAL PRIMARY KEY,
    user_id               VARCHAR(36)  NOT NULL,
    label                 VARCHAR(200) NOT NULL,
    access_token          TEXT         NOT NULL,
    refresh_token         TEXT         NOT NULL,
    expiration_time       TIMESTAMP,
    allowed_ips           TEXT,
    token_expiration_time TIMESTAMP    NOT NULL,
    key                   VARCHAR(36)  NOT NULL UNIQUE,
    is_enabled            BOOLEAN      NOT NULL DEFAULT true,
    is_expired            BOOLEAN      NOT NULL DEFAULT false
);
CREATE TABLE IF NOT EXISTS rate_limit_group
(
    id                     BIGSERIAL PRIMARY KEY,
    name                   VARCHAR(50) NOT NULL,
    request_count          INT         NOT NULL,
    request_window_seconds INT         NOT NULL,
    cooldown_seconds       INT         NOT NULL,
    max_penalty_level      INT         NOT NULL,
    enabled                BOOLEAN     NOT NULL
);

CREATE TABLE IF NOT EXISTS rate_limit_penalty
(
    id                     BIGSERIAL PRIMARY KEY,
    group_id               BIGINT NOT NULL REFERENCES rate_limit_group (id),
    block_step        INT    NOT NULL,
    block_duration_seconds INT    NOT NULL,
    unique (group_id, block_step)
);

CREATE TABLE IF NOT EXISTS rate_limit_endpoint
(
    id          BIGSERIAL PRIMARY KEY,
    url VARCHAR(255) NOT NULL,
    method VARCHAR(10)  NOT NULL,
    group_id    BIGINT       NOT NULL REFERENCES rate_limit_group (id),
    priority    INT          NOT NULL,
    enabled     BOOLEAN      NOT NULL,
    unique (url, method)
);
