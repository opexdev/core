CREATE TABLE IF NOT EXISTS address_types (
   id SERIAL PRIMARY KEY,
   address_type VARCHAR(20) NOT NULL,
   address_regex VARCHAR(72) NOT NULL,
   memo_regex VARCHAR(72)
);

CREATE TABLE IF NOT EXISTS assigned_addresses (
   id SERIAL PRIMARY KEY,
   uuid VARCHAR(72) NOT NULL,
   address VARCHAR(72) NOT NULL,
   memo VARCHAR(72) NOT NULL,
   addr_type_id INTEGER NOT NULL REFERENCES address_types (id),
   UNIQUE (address, memo)
);

CREATE TABLE IF NOT EXISTS reserved_addresses (
   id SERIAL PRIMARY KEY,
   address VARCHAR(72) NOT NULL,
   memo VARCHAR(72) NOT NULL,
   address_type INTEGER NOT NULL REFERENCES address_types (id),
   UNIQUE (address, memo)
);

CREATE TABLE IF NOT EXISTS chains (name VARCHAR(72) PRIMARY KEY);

CREATE TABLE IF NOT EXISTS assigned_address_chains (
   id SERIAL PRIMARY KEY,
   assigned_address_id INTEGER NOT NULL REFERENCES assigned_addresses (id),
   chain VARCHAR(72) NOT NULL REFERENCES chains (name)
);

CREATE TABLE IF NOT EXISTS chain_address_types (
   id SERIAL PRIMARY KEY,
   chain_name VARCHAR(72) NOT NULL REFERENCES chains (name),
   addr_type_id INTEGER NOT NULL REFERENCES address_types (id)
);

CREATE TABLE IF NOT EXISTS chain_endpoints (
   id SERIAL PRIMARY KEY,
   chain_name VARCHAR(72) NOT NULL REFERENCES chains (name),
   endpoint_url VARCHAR(255) NOT NULL,
   endpoint_user VARCHAR(72),
   endpoint_password VARCHAR(72)
);

CREATE TABLE IF NOT EXISTS chain_sync_schedules (
   chain VARCHAR(72) PRIMARY KEY REFERENCES chains (name),
   retry_time TIMESTAMP NOT NULL,
   delay INTEGER NOT NULL,
   error_delay INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS chain_sync_retry (
    id SERIAL PRIMARY KEY,
    chain VARCHAR(72) REFERENCES chains (name),
    block INTEGER NOT NULL,
    retries INTEGER NOT NULL DEFAULT 1,
    synced BOOLEAN NOT NULL DEFAULT false,
    give_up BOOLEAN NOT NULL DEFAULT false,
    error TEXT,
    UNIQUE (chain, block)
);

CREATE TABLE IF NOT EXISTS chain_sync_records (
   chain VARCHAR(72) PRIMARY KEY REFERENCES chains (name),
   time TIMESTAMP NOT NULL,
   endpoint_url VARCHAR(72) NOT NULL,
   latest_block INTEGER,
   success BOOLEAN NOT NULL,
   error VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS wallet_sync_schedules (
   id INTEGER PRIMARY KEY DEFAULT(1) CHECK(id = 1),
   retry_time TIMESTAMP NOT NULL,
   delay INTEGER NOT NULL,
   batch_size INTEGER
);

CREATE TABLE IF NOT EXISTS wallet_sync_records (
   id SERIAL PRIMARY KEY,
   time TIMESTAMP NOT NULL,
   success BOOLEAN NOT NULL,
   error TEXT
);

CREATE TABLE IF NOT EXISTS deposits (
   id SERIAL PRIMARY KEY,
   hash TEXT UNIQUE NOT NULL,
   wallet_record_id INTEGER REFERENCES wallet_sync_records (id),
   chain VARCHAR(72) NOT NULL REFERENCES chains (name),
   token BOOLEAN NOT NULL,
   token_address VARCHAR(72),
   amount DECIMAL NOT NULL,
   depositor VARCHAR(72) NOT NULL,
   depositor_memo VARCHAR(72)
);

CREATE TABLE IF NOT EXISTS currency (
   symbol VARCHAR(72) PRIMARY KEY,
   name VARCHAR(72) NOT NULL
);

CREATE TABLE IF NOT EXISTS currency_implementations (
   id SERIAL PRIMARY KEY,
   symbol VARCHAR(72) NOT NULL,
   chain VARCHAR(72) NOT NULL REFERENCES chains (name),
   token BOOLEAN NOT NULL,
   token_address VARCHAR(72),
   token_name VARCHAR(72),
   withdraw_enabled BOOLEAN NOT NULL,
   withdraw_fee DECIMAL NOT NULL,
   withdraw_min DECIMAL NOT NULL,
   decimal INTEGER NOT NULL
);
