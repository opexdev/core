package co.nilin.opex.port.api.postgres.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.core.DatabaseClient

@Configuration
@Profile("demo")
@EnableR2dbcRepositories(basePackages = ["co.nilin.opex"])
class PostgresDemoConfig(db: DatabaseClient) {

    init {
        val sql =
            """
            INSERT INTO symbol_maps(symbol, value) VALUES('btc_usdt', 'BTCUSDT') ON CONFLICT DO NOTHING; 
            INSERT INTO symbol_maps(symbol, value) VALUES('eth_usdt', 'ETHUSDT') ON CONFLICT DO NOTHING; 
            INSERT INTO symbol_maps(symbol, value) VALUES('eth_btc', 'ETHBTC') ON CONFLICT DO NOTHING; 
            INSERT INTO symbol_maps(symbol, value) VALUES('nln_usdt', 'NLNUSDT') ON CONFLICT DO NOTHING; 
            INSERT INTO symbol_maps(symbol, value) VALUES('nln_btc', 'NLNBTC') ON CONFLICT DO NOTHING; 
            """
        val initDb = db.sql { sql }
        initDb // initialize the database
            .then()
            .subscribe() // execute
    }
}
