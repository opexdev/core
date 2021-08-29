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
                    address_type VARCHAR(72),
                    address_regex VARCHAR(72),
                    memo_regex VARCHAR(72)
                );
                CREATE TABLE IF NOT EXISTS assigned_addresses (
                    id SERIAL PRIMARY KEY,
                    uuid VARCHAR(72) UNIQUE,
                    address VARCHAR(72),
                    memo VARCHAR(72),
                    addr_type_id numeric,
                    UNIQUE (address, memo)
                );
                CREATE TABLE IF NOT EXISTS assigned_address_chains (
                    id SERIAL PRIMARY KEY,
                    assigned_address_id numeric,
                    chain VARCHAR(72) 
                );
                CREATE TABLE IF NOT EXISTS reserved_addresses (
                    id SERIAL PRIMARY KEY,
                    address VARCHAR(72),
                    memo VARCHAR(72),
                    address_type VARCHAR(72),
                    UNIQUE (address, memo)
                );
                CREATE TABLE IF NOT EXISTS chain (
                    name VARCHAR(72) PRIMARY KEY
                );
                CREATE TABLE IF NOT EXISTS chain_address_types (
                    id SERIAL PRIMARY KEY,
                    chain_name VARCHAR(72),
                    addr_type_id numeric
                );
                CREATE TABLE IF NOT EXISTS chain_endpoints (
                    id SERIAL PRIMARY KEY,
                    chain_name VARCHAR(72),
                    endpoint_url VARCHAR(72),
                    endpoint_user VARCHAR(72),
                    endpoint_password VARCHAR(72)
                );
                CREATE TABLE IF NOT EXISTS chain_sync_schedule (
                    chain VARCHAR(72) PRIMARY KEY,
                    retry_time TIMESTAMP,
                    delay numeric
                );
                CREATE TABLE IF NOT EXISTS chain_sync_record (
                    chain VARCHAR(72) PRIMARY KEY,
                    time TIMESTAMP,
                    endpoint_url VARCHAR(72),
                    latest_block numeric,
                    success BOOLEAN,
                    error VARCHAR(100)
                );
            """
        }
        initDb // initialize the database
            .then()
            .subscribe() // execute
    }
}
