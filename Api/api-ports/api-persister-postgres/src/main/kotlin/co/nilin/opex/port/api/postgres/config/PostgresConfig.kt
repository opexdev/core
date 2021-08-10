package co.nilin.opex.port.api.postgres.config

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
                client_order_id VARCHAR(72),
                symbol VARCHAR(20),
                order_id numeric,
                maker_fee decimal,
                taker_fee decimal,
                left_side_fraction decimal,
                right_side_fraction decimal,
                user_level VARCHAR(20),
                side VARCHAR(20),
                match_constraint VARCHAR(20),
                order_type VARCHAR(20),
                price decimal,
                quantity decimal,
                quote_quantity decimal,
                executed_qty decimal,
                accumulative_quote_qty decimal,
                status integer,
                create_date TIMESTAMP,
                update_date TIMESTAMP
            );
            CREATE TABLE IF NOT EXISTS trades (
                id SERIAL PRIMARY KEY,
                trade_id numeric,
                symbol VARCHAR(20),
                matched_quantity decimal,
                taker_price decimal,
                maker_price decimal,
                taker_commision decimal,
                maker_commision decimal,
                taker_commision_asset VARCHAR(20),
                maker_commision_asset VARCHAR(20),
                trade_date TIMESTAMP,
                maker_ouid VARCHAR(72) NOT NULL,
                taker_ouid VARCHAR(72) NOT NULL,
                maker_uuid VARCHAR(72) NOT NULL,
                taker_uuid VARCHAR(72) NOT NULL,
                create_date TIMESTAMP
            );
        """
        val initDb = db.sql { sql }
        initDb // initialize the database
                .then()
                .subscribe() // execute
    }
}
