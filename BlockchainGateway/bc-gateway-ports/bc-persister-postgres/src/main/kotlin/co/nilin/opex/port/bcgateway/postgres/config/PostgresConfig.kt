package co.nilin.opex.port.bcgateway.postgres.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.core.DatabaseClient

@Configuration
@EnableR2dbcRepositories(basePackages = ["co.nilin.opex"])
class PostgresConfig(db: DatabaseClient) {

    init {
        val initDb = db.sql {
            """ 
                CREATE TABLE IF NOT EXISTS address_types (
                    id SERIAL PRIMARY KEY,
                    address_type VARCHAR(72) NOT NULL,
                    address_regex VARCHAR(72) NOT NULL,
                    memo_regex VARCHAR(72) NOT NULL
                );
                CREATE TABLE IF NOT EXISTS assigned_addresses (
                    id SERIAL PRIMARY KEY,
                    uuid VARCHAR(72) NOT NULL,
                    address VARCHAR(72) NOT NULL,
                    memo VARCHAR(72),
                    addr_type_id INTEGER NOT NULL,
                    UNIQUE (address, memo)
                );
                CREATE TABLE IF NOT EXISTS assigned_address_chains (
                    id SERIAL PRIMARY KEY,
                    assigned_address_id INTEGER NOT NULL,
                    chain VARCHAR(72) NOT NULL
                );
                CREATE TABLE IF NOT EXISTS reserved_addresses (
                    id SERIAL PRIMARY KEY,
                    address VARCHAR(72) NOT NULL,
                    memo VARCHAR(72),
                    address_type VARCHAR(72) NOT NULL,
                    UNIQUE (address, memo)
                );
                CREATE TABLE IF NOT EXISTS chains (
                    name VARCHAR(72) PRIMARY KEY
                );
                CREATE TABLE IF NOT EXISTS chain_address_types (
                    id SERIAL PRIMARY KEY,
                    chain_name VARCHAR(72) NOT NULL REFERENCES chains (name),
                    addr_type_id INTEGER NOT NULL REFERENCES address_types (id)
                );
                CREATE TABLE IF NOT EXISTS chain_endpoints (
                    id SERIAL PRIMARY KEY,
                    chain_name VARCHAR(72) NOT NULL,
                    endpoint_url VARCHAR(72) NOT NULL,
                    endpoint_user VARCHAR(72),
                    endpoint_password VARCHAR(72)
                );
                CREATE TABLE IF NOT EXISTS chain_sync_schedules (
                    chain VARCHAR(72) PRIMARY KEY,
                    retry_time TIMESTAMP NOT NULL,
                    delay NUMERIC NOT NULL
                );
                CREATE TABLE IF NOT EXISTS chain_sync_records (
                    chain VARCHAR(72) PRIMARY KEY,
                    time TIMESTAMP NOT NULL,
                    endpoint_url VARCHAR(72) NOT NULL,
                    latest_block INTEGER,
                    success BOOLEAN NOT NULL,
                    error VARCHAR(100)
                );
                CREATE TABLE IF NOT EXISTS currency (
                    symbol VARCHAR(72) PRIMARY KEY,
                    name VARCHAR(72) NOT NULL
                );
                CREATE TABLE IF NOT EXISTS currency_implementations (
                    symbol VARCHAR(72) PRIMARY KEY,
                    chain VARCHAR(72) NOT NULL,
                    token BOOLEAN NOT NULL,
                    token_address VARCHAR(72),
                    token_name VARCHAR(72),
                    withdraw_enabled BOOLEAN NOT NULL,
                    withdraw_fee NUMERIC NOT NULL,
                    withdraw_min NUMERIC NOT NULL
                );
                CREATE TABLE IF NOT EXISTS deposits (
                    id SERIAL PRIMARY KEY,
                    chain VARCHAR(72),
                    token BOOLEAN NOT NULL,
                    token_address VARCHAR(72),
                    amount NUMERIC NOT NULL,
                    depositor VARCHAR(72) NOT NULL,
                    depositorMemo VARCHAR(72)
                );
            """
        }
        initDb // initialize the database
            .then()
            .subscribe() // execute
    }
}
