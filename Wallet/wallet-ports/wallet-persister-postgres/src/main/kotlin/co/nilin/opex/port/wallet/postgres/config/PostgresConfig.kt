package co.nilin.opex.port.wallet.postgres.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.core.DatabaseClient

@Configuration
@EnableR2dbcRepositories(basePackages = ["co.nilin.opex"])
class PostgresConfig(db: DatabaseClient) {

    init {
        val initDb = db.sql {
            """ 
                CREATE TABLE IF NOT EXISTS currency (
                    name VARCHAR(25) PRIMARY KEY,
                    symbol VARCHAR(25) NOT NULL UNIQUE,
                    precision numeric NOT NULL
                );
                
                CREATE TABLE IF NOT EXISTS currency_rate (
                    id SERIAL PRIMARY KEY,
                    source_currency VARCHAR(25) NOT NULL,
                    dest_currency VARCHAR(25) NOT NULL,
                    rate decimal
                );  
                 
                CREATE TABLE IF NOT EXISTS transaction (
                    id SERIAL PRIMARY KEY,
                    source_wallet numeric NOT NULL,
                    dest_wallet numeric NOT NULL,
                    source_amount decimal NOT NULL,
                    dest_amount decimal NOT NULL,
                    description VARCHAR(100),
                    transfer_ref VARCHAR(25),
                    transaction_date DATE NOT NULL DEFAULT CURRENT_DATE
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
                    owner numeric NOT NULL,
                    wallet_type VARCHAR(10),
                    currency VARCHAR(25) NOT NULL,
                    balance decimal
                );
                  
                CREATE TABLE IF NOT EXISTS user_limits (
                    id SERIAL PRIMARY KEY,
                    level VARCHAR(10),
                    owner numeric,
                    action VARCHAR(25) NOT NULL,
                    wallet_type VARCHAR(10) NOT NULL,
                    daily_total decimal,
                    daily_count numeric,
                    monthly_total decimal,
                    monthly_count numeric
                );  
                
                CREATE TABLE IF NOT EXISTS wallet_limits (
                    id SERIAL PRIMARY KEY,
                    level VARCHAR(10),
                    owner numeric,
                    action VARCHAR(25) NOT NULL,
                    currency VARCHAR(25) NOT NULL,
                    wallet_type VARCHAR(10) NOT NULL,
                    wallet_id numeric,
                    daily_total decimal,
                    daily_count numeric,
                    monthly_total decimal,
                    monthly_count numeric
                );  
                
                CREATE TABLE IF NOT EXISTS wallet_config (
                    name VARCHAR(20) PRIMARY KEY,
                    main_currency VARCHAR(25) NOT NULL
                );

                insert into wallet_owner(id, uuid, title, level) values(1, '1', 'system', 'basic') ON CONFLICT DO NOTHING; 
                insert into user_limits (id, level, owner, action, wallet_type, daily_total, daily_count, monthly_total, monthly_count)values(1, null, 1, 'withdraw', 'main', 1000, 100, 10000, 1000) ON CONFLICT DO NOTHING;
            """
        }
        initDb // initialize the database
                .then()
                .subscribe() // execute
    }
}
