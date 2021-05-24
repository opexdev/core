package co.nilin.mixchange.port.order.kafka.config


import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.core.DatabaseClient


@Configuration
@EnableR2dbcRepositories(basePackages = ["co.nilin.mixchange"])
class PostgresConfig(db: DatabaseClient) {

    init {
        val initDb = db.sql {
            """ 
                drop table IF EXISTS wallet_owner;
                drop table IF EXISTS wallet;
                drop table IF EXISTS transaction;
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
                    level VARCHAR(10) NOT NULL
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
                insert into currency(name, symbol, precision) values('btc', 'btc', 0.000001) ON CONFLICT DO NOTHING;; 
                insert into currency(name, symbol, precision) values('usdt', 'usdt', 0.01) ON CONFLICT DO NOTHING;; 
                insert into currency(name, symbol, precision) values('nln', 'nln', 1) ON CONFLICT DO NOTHING;; 
                insert into currency_rate(id, source_currency, dest_currency, rate) values(1, 'btc', 'nln', 5500000) ON CONFLICT DO NOTHING;
                insert into currency_rate(id, source_currency, dest_currency, rate) values(1, 'usdt', 'nln', 100) ON CONFLICT DO NOTHING;
                insert into currency_rate(id, source_currency, dest_currency, rate) values(1, 'btc', 'usdt', 55000) ON CONFLICT DO NOTHING;
                insert into wallet(id, owner, wallet_type, currency, balance) values(1, 1, 'main', 'btc', 10) ON CONFLICT DO NOTHING;
                insert into wallet(id, owner, wallet_type, currency, balance) values(2, 1, 'exchange', 'btc', 0) ON CONFLICT DO NOTHING; 
                insert into wallet(id, owner, wallet_type, currency, balance) values(3, 1, 'main', 'usdt', 550000) ON CONFLICT DO NOTHING; 
                insert into wallet(id, owner, wallet_type, currency, balance) values(4, 1, 'exchange', 'usdt', 0) ON CONFLICT DO NOTHING; 
                insert into wallet(id, owner, wallet_type, currency, balance) values(5, 1, 'main', 'nln', 100000000) ON CONFLICT DO NOTHING; 
                insert into wallet(id, owner, wallet_type, currency, balance) values(6, 1, 'exchange', 'nln', 0) ON CONFLICT DO NOTHING; 
                insert into user_limits (id, level, owner, action, wallet_type, daily_total, daily_count, monthly_total, monthly_count)values(1, null, 1, 'withdraw', 'main', 1000, 100, 10000, 1000) ON CONFLICT DO NOTHING;
            """
        }
        initDb // initialize the database
                .then()
                .subscribe() // execute
    }
}
