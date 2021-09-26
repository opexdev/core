package co.nilin.opex.port.accountant.postgres.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.core.DatabaseClient

@Configuration
@EnableR2dbcRepositories(basePackages = ["co.nilin.opex"])
class PostgresConfig(db: DatabaseClient) {

    init {
        val sql = """ 
                CREATE TABLE IF NOT EXISTS orders (
                    id SERIAL PRIMARY KEY,
                    ouid VARCHAR(72) NOT NULL UNIQUE,
                    uuid VARCHAR(72) NOT NULL,
                    pair VARCHAR(20),
                    matching_engine_id numeric,
                    maker_fee decimal,
                    taker_fee decimal,
                    left_side_fraction decimal,
                    right_side_fraction decimal,
                    user_level VARCHAR(20),
                    direction VARCHAR(20),
                    match_constraint VARCHAR(30),
                    order_type VARCHAR(30),
                    price numeric,
                    quantity numeric,
                    filled_quantity numeric,
                    orig_price decimal,
                    orig_quantity decimal,
                    filled_orig_quantity decimal,
                    first_transfer_amount numeric,
                    remained_transfer_amount numeric,
                    status integer,
                    agent VARCHAR(20),
                    ip VARCHAR(11),
                    create_date TIMESTAMP
                );
                
                 CREATE TABLE IF NOT EXISTS fi_actions (
                    id SERIAL PRIMARY KEY,
                    parent_id numeric,
                    event_type VARCHAR(72) NOT NULL,
                    pointer VARCHAR(72) NOT NULL,
                    symbol VARCHAR(36),
                    amount decimal,
                    sender VARCHAR(36),
                    sender_wallet_type VARCHAR(36),
                    receiver VARCHAR(36),
                    receiver_wallet_type VARCHAR(36),                    
                    agent VARCHAR(20),
                    ip VARCHAR(11),
                    create_date TIMESTAMP,
                    status VARCHAR(20),
                    retry_count numeric,
                    last_try_date TIMESTAMP
                );   
                CREATE TABLE IF NOT EXISTS pair_config (
                    pair VARCHAR(72) PRIMARY KEY,
                    left_side_wallet_symbol VARCHAR(36) NOT NULL,
                    right_side_wallet_symbol VARCHAR(36) NOT NULL,
                    left_side_fraction decimal,
                    right_side_fraction decimal,
                    rate decimal
                );  
                CREATE TABLE IF NOT EXISTS pair_fee_config (
                    id SERIAL PRIMARY KEY,
                    pair_config_id VARCHAR(72),
                    direction VARCHAR(36) NOT NULL,
                    user_level VARCHAR(36) NOT NULL,
                    maker_fee decimal,
                    taker_fee decimal
                );
                
                 CREATE TABLE IF NOT EXISTS temp_events (
                    id SERIAL PRIMARY KEY,
                    ouid VARCHAR(72) NOT NULL,
                    event_type VARCHAR(72) NOT NULL,
                    event_body TEXT,
                    event_date TIMESTAMP
                );  
                commit;      
            """
        val initDb = db.sql { sql }
        initDb // initialize the database
            .then()
            .subscribe() // execute
    }
}
