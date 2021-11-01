CREATE TABLE IF NOT EXISTS currency (
   symbol VARCHAR(25) PRIMARY KEY,
   name VARCHAR(25),
   precision DECIMAL NOT NULL
);

CREATE TABLE IF NOT EXISTS currency_rate (
   id SERIAL PRIMARY KEY,
   source_currency VARCHAR(25) NOT NULL REFERENCES currency (symbol),
   dest_currency VARCHAR(25) NOT NULL REFERENCES currency (symbol),
   rate DECIMAL NOT NULL,
   UNIQUE(source_currency, dest_currency)
);

CREATE TABLE IF NOT EXISTS transaction (
   id SERIAL PRIMARY KEY,
   source_wallet INTEGER NOT NULL REFERENCES wallet (id),
   dest_wallet INTEGER NOT NULL REFERENCES wallet (id),
   source_amount DECIMAL NOT NULL,
   dest_amount DECIMAL NOT NULL,
   description TEXT,
   transfer_ref TEXT,
   transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE IF NOT EXISTS wallet_owner (
   id SERIAL PRIMARY KEY,
   uuid VARCHAR(36) NOT NULL UNIQUE,
   title VARCHAR(70) NOT NULL,
   level VARCHAR(10) NOT NULL,
   trade_allowed BOOLEAN NOT NULL DEFAULT true,
   withdraw_allowed BOOLEAN NOT NULL DEFAULT true,
   deposit_allowed BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS wallet (
   id SERIAL PRIMARY KEY,
   owner INTEGER NOT NULL REFERENCES wallet_owner (id),
   wallet_type VARCHAR(10) NOT NULL,
   currency VARCHAR(25) NOT NULL REFERENCES currency (symbol),
   balance DECIMAL NOT NULL
);

CREATE TABLE IF NOT EXISTS user_limits (
   id SERIAL PRIMARY KEY,
   level VARCHAR(10),
   owner INTEGER REFERENCES wallet_owner (id),
   action VARCHAR(25) NOT NULL,
   wallet_type VARCHAR(10) NOT NULL,
   daily_total DECIMAL,
   daily_count INTEGER,
   monthly_total DECIMAL,
   monthly_count INTEGER
);

CREATE TABLE IF NOT EXISTS wallet_limits (
   id SERIAL PRIMARY KEY,
   level VARCHAR(10),
   owner INTEGER REFERENCES wallet_owner (id),
   action VARCHAR(25) NOT NULL,
   currency VARCHAR(25) NOT NULL REFERENCES currency (symbol),
   wallet_type VARCHAR(10) NOT NULL,
   wallet_id INTEGER REFERENCES wallet (id),
   daily_total DECIMAL,
   daily_count INTEGER,
   monthly_total DECIMAL,
   monthly_count INTEGER
);

CREATE TABLE IF NOT EXISTS wallet_config (
   name VARCHAR(20) PRIMARY KEY,
   main_currency VARCHAR(25) NOT NULL REFERENCES currency (symbol)
);

CREATE TABLE IF NOT EXISTS withdraws (
   id SERIAL PRIMARY KEY,
   uuid VARCHAR(36) NOT NULL,
   req_transaction_id VARCHAR(20) NOT NULL UNIQUE,
   final_transaction_id VARCHAR(20) UNIQUE,
   wallet INTEGER REFERENCES wallet (id),
   amount DECIMAL,
   accepted_fee DECIMAL,
   applied_fee DECIMAL,
   dest_amount DECIMAL,
   dest_currency VARCHAR(20) REFERENCES currency (symbol),
   dest_network VARCHAR(20),
   dest_address VARCHAR(80),
   dest_notes TEXT,
   dest_transaction_ref VARCHAR(100),
   description TEXT,
   status_reason TEXT,
   status VARCHAR(20),
   create_date TIMESTAMP NOT NULL,
   accept_date TIMESTAMP
);
