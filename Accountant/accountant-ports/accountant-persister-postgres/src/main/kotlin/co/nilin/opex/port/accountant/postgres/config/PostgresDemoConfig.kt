package co.nilin.opex.port.accountant.postgres.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.core.DatabaseClient

@Configuration
@Profile("demo")
@EnableR2dbcRepositories(basePackages = ["co.nilin.opex"])
class PostgresDemoConfig(db: DatabaseClient) {

    init {
        val sql = """ 
            insert into pair_config values('btc_usdt', 'btc', 'usdt', 0.000001, 0.01, 55000)ON CONFLICT DO NOTHING;
            insert into pair_fee_config values(1, 'btc_usdt', 'ASK', '*', 0.01, 0.01) ON CONFLICT DO NOTHING;         
            insert into pair_fee_config values(2, 'btc_usdt', 'BID', '*', 0.01, 0.01) ON CONFLICT DO NOTHING;
            insert into pair_config values('nln_usdt', 'nln', 'usdt', 1.0, 0.01, 0.01) ON CONFLICT DO NOTHING;         
            insert into pair_fee_config values(3, 'nln_usdt', 'ASK', '*', 0.01, 0.01) ON CONFLICT DO NOTHING;         
            insert into pair_fee_config values(4, 'nln_usdt', 'BID', '*', 0.01, 0.01) ON CONFLICT DO NOTHING;
            insert into pair_config values('nln_btc', 'nln', 'btc', 1.0, 0.000001, 1/5500000) ON CONFLICT DO NOTHING;         
            insert into pair_fee_config values(5, 'nln_btc', 'ASK', '*', 0.01, 0.01) ON CONFLICT DO NOTHING;         
            insert into pair_fee_config values(6, 'nln_btc', 'BID', '*', 0.01, 0.01) ON CONFLICT DO NOTHING;   
            commit;      
            """
        val initDb = db.sql { sql }
        initDb // initialize the database
            .then()
            .subscribe() // execute
    }

}