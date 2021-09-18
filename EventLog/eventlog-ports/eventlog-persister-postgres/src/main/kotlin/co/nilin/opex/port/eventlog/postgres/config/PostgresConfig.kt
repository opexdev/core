package co.nilin.opex.port.eventlog.postgres.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.core.DatabaseClient

@Configuration
@EnableR2dbcRepositories(basePackages = ["co.nilin.opex"])
class PostgresConfig(db: DatabaseClient) {

    init {
        val initDb = db.sql {
            """ CREATE TABLE IF NOT EXISTS opex_orders (
                    id SERIAL PRIMARY KEY,
                    ouid VARCHAR(72) NOT NULL UNIQUE,
                    symbol VARCHAR(20),
                    direction VARCHAR(20),
                    match_constraint VARCHAR(20),
                    order_type VARCHAR(20),
                    uuid VARCHAR(72) NOT NULL,
                    agent VARCHAR(20),
                    ip VARCHAR(11),
                    order_date TIMESTAMP,
                    create_date TIMESTAMP
                );
                CREATE TABLE IF NOT EXISTS opex_order_events (
                    id SERIAL PRIMARY KEY,
                    ouid VARCHAR(72) NOT NULL,
                    matching_orderid bigint,
                    price bigint,
                    quantity bigint,
                    filled_quantity bigint,
                    uuid VARCHAR(72) NOT NULL,
                    event VARCHAR(30) NOT NULL,
                    agent VARCHAR(20),
                    ip VARCHAR(11),
                    event_date TIMESTAMP,
                    create_date TIMESTAMP
                );
                CREATE TABLE IF NOT EXISTS opex_events (
                    id SERIAL PRIMARY KEY,
                    correlation_id VARCHAR(72), 
                    ouid VARCHAR(72),          
                    uuid VARCHAR(72),
                    symbol VARCHAR(20),
                    event VARCHAR(30) NOT NULL,
                    event_json TEXT NOT NULL,
                    agent VARCHAR(20),
                    ip VARCHAR(11),
                    event_date TIMESTAMP,
                    create_date TIMESTAMP
                );
                CREATE TABLE IF NOT EXISTS opex_trades (
                    id SERIAL PRIMARY KEY,
                    symbol VARCHAR(20),
                    taker_ouid VARCHAR(72) NOT NULL,
                    taker_uuid VARCHAR(72) NOT NULL,
                    taker_matching_orderid bigint,
                    taker_direction VARCHAR(20),
                    taker_price bigint,
                    taker_remained_quantity bigint,
                    maker_ouid VARCHAR(72) NOT NULL,
                    maker_uuid VARCHAR(72) NOT NULL,
                    maker_matching_orderid bigint,
                    maker_direction VARCHAR(20),
                    maker_price bigint,
                    maker_remained_quantity bigint,
                    matched_quantity bigint,
                    trade_date TIMESTAMP,
                    create_date TIMESTAMP
                );
            """
        }

        initDb // initialize the database
                .then()
                .subscribe() // execute
    }

}
