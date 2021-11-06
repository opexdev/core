package co.nilin.opex.port.bcgateway.postgres.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.r2dbc.core.DatabaseClient

@Configuration
@Profile("demo")
class DemoPostgresConfig(db: DatabaseClient) {

    init {
        val initDb = db.sql {
            """ 
                insert into address_types values 
                    (1, 'bitcoin', '', ''),
                    (2, 'ethereum', '', '')
                ON CONFLICT DO NOTHING;
                
                insert into chains values 
                    ('bitcoin-testnet'),
                    ('ethereum-ropsten'),
                    ('bsc-ropsten')
                ON CONFLICT DO NOTHING;
                
                insert into chain_address_types values 
                    (1, 'bitcoin-testnet', 1),
                    (2, 'ethereum-ropsten', 2),
                    (3, 'bsc-ropsten', 2)
                ON CONFLICT DO NOTHING;
                
                insert into currency values 
                    ('BTC', 'Bitcoin'),
                    ('ETH', 'Ethereum'),
                    ('USDT', 'Tether')
                ON CONFLICT DO NOTHING;
                
                insert into currency_implementations values
                    (1, 'BTC', 'bitcoin-testnet', false, null, null, true, 0.00001, 0.00001, 0), 
                    (2, 'ETH', 'ethereum-ropsten', false, null, null, true, 0.0001, 0.0001, 18),
                    (3, 'USDT', 'ethereum-ropsten', true, '0x110a13fc3efe6a245b50102d2d79b3e76125ae83', 'USDT', true, 0.01, 0.01, 6)
                ON CONFLICT DO NOTHING;
                
                insert into chain_endpoints (id, chain_name, endpoint_url) values 
                    (1, 'bitcoin-testnet', 'http://host.docker.internal:9990/bitcoin/transfers'),
                    (2, 'ethereum-ropsten', 'http://host.docker.internal:9990/eth/transfers')
                ON CONFLICT DO NOTHING;
                
                insert into chain_sync_schedules values 
                    ('bitcoin-testnet', CURRENT_DATE, 600),
                    ('ethereum-ropsten', CURRENT_DATE, 90)
                ON CONFLICT DO NOTHING;
                
                insert into wallet_sync_schedules values (1, CURRENT_DATE, 30, 10000) ON CONFLICT DO NOTHING;
            """
        }
        initDb // initialize the database
            .then()
            .subscribe() // execute
    }
}
