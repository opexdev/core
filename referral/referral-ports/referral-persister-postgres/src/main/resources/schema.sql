CREATE TABLE IF NOT EXISTS configs (
    name VARCHAR(72) PRIMARY KEY,
    referral_commission_reward DECIMAL NOT NULL,
    payment_asset_symbol VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS referral_codes (
    id SERIAL PRIMARY KEY,
    uuid VARCHAR(72) NOT NULL,
    code VARCHAR(255) NOT NULL UNIQUE,
    referent_commission DECIMAL NOT NULL
);

CREATE TABLE IF NOT EXISTS referral_code_references (
    id SERIAL PRIMARY KEY,
    referent_uuid VARCHAR(72) NOT NULL UNIQUE,
    referral_code_id INTEGER NOT NULL REFERENCES referral_codes(id),
    UNIQUE(referent_uuid, referral_code_id)
);

CREATE TABLE IF NOT EXISTS payment_status (
    status VARCHAR(20) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS commission_rewards (
    id BIGSERIAL PRIMARY KEY,
    rewarded_uuid VARCHAR(72) NOT NULL,
    referent_uuid VARCHAR(72) NOT NULL,
    referral_code VARCHAR(72) NOT NULL REFERENCES referral_codes(code),
    rich_trade_id BIGINT NOT NULL,
    referent_order_direction VARCHAR(20) NOT NULL,
    share DECIMAL NOT NULL,
    payment_asset_symbol VARCHAR(20) NOT NULL,
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT reward_once_constraint UNIQUE (rich_trade_id, rewarded_uuid, referent_order_direction)
);

CREATE TABLE IF NOT EXISTS payment_records (
    id BIGSERIAL PRIMARY KEY,
    commission_rewards_id BIGINT NOT NULL REFERENCES commission_rewards(id),
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'pending' REFERENCES payment_status(status)
);

CREATE INDEX IF NOT EXISTS payment_records_status_index ON payment_records(payment_status);

CREATE OR REPLACE VIEW payment_records_projected SELECT DISTINCT ON (commission_rewards_id) * FROM payment_records LEFT JOIN commission_rewards ON commission_rewards_id = commission_rewards.id ORDER BY create_date DESC;

CREATE OR REPLACE FUNCTION on_insert_commission_rewards() RETURNS TRIGGER AS $$ BEGIN
    INSERT INTO payment_records(commission_rewards_id) VALUES (NEW.id);
    RETURN NEW;
END; $$ LANGUAGE 'plpgsql';

CREATE OR REPLACE TRIGGER commission_rewards_insert AFTER INSERT
ON commission_rewards
FOR EACH ROW
EXECUTE PROCEDURE on_insert_commission_rewards();
