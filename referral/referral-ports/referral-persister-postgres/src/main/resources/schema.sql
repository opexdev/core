CREATE TABLE IF NOT EXISTS configs (
    name VARCHAR(72) PRIMARY KEY,
    referral_commission_reward DECIMAL NOT NULL,
    payment_currency VARCHAR(20) NOT NULL,
    min_payment_amount DECIMAL NOT NULL,
    payment_window_seconds INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS referral_codes (
    id SERIAL PRIMARY KEY,
    uuid VARCHAR(72) NOT NULL,
    code VARCHAR(255) NOT NULL UNIQUE,
    referent_commission DECIMAL NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS code_index ON referral_codes(code);

CREATE TABLE IF NOT EXISTS referral_code_references (
    id SERIAL PRIMARY KEY,
    referent_uuid VARCHAR(72) NOT NULL UNIQUE,
    referral_code_id INTEGER NOT NULL REFERENCES referral_codes(id),
    UNIQUE(referent_uuid, referral_code_id)
);

CREATE TABLE IF NOT EXISTS checkout_states (
    state VARCHAR(20) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS commission_rewards (
    id BIGSERIAL PRIMARY KEY,
    rewarded_uuid VARCHAR(72) NOT NULL,
    referent_uuid VARCHAR(72) NOT NULL,
    referral_code VARCHAR(72) NOT NULL REFERENCES referral_codes(code),
    rich_trade_id BIGINT NOT NULL,
    referent_order_direction VARCHAR(20) NOT NULL,
    share DECIMAL NOT NULL,
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT reward_once_constraint UNIQUE (rich_trade_id, rewarded_uuid, referent_order_direction)
);

CREATE TABLE IF NOT EXISTS checkout_records (
    id BIGSERIAL PRIMARY KEY,
    commission_rewards_id BIGINT NOT NULL REFERENCES commission_rewards(id),
    transfer_ref VARCHAR(255),
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    checkout_state VARCHAR(20) NOT NULL DEFAULT 'PENDING' REFERENCES checkout_states(state)
);

CREATE INDEX IF NOT EXISTS checkout_records_state_index ON checkout_records(checkout_state);

DROP VIEW IF EXISTS checkout_records_projected;

CREATE VIEW checkout_records_projected
AS SELECT DISTINCT ON (commission_rewards_id)
    checkout_records.id,
    commission_rewards.id AS commission_rewards_id,
    rewarded_uuid,
    referent_uuid,
    referral_code,
    rich_trade_id,
    referent_order_direction,
    share,
    checkout_state,
    transfer_ref,
    create_date,
    update_date
FROM checkout_records
LEFT JOIN commission_rewards
ON commission_rewards_id = commission_rewards.id
ORDER BY commission_rewards_id, update_date DESC;

CREATE OR REPLACE FUNCTION on_insert_commission_rewards() RETURNS TRIGGER AS $$ BEGIN
    INSERT INTO checkout_records(commission_rewards_id) VALUES (NEW.id);
    RETURN NEW;
END; $$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS commission_rewards_insert ON commission_rewards CASCADE;

CREATE TRIGGER commission_rewards_insert AFTER INSERT
ON commission_rewards
FOR EACH ROW
EXECUTE PROCEDURE on_insert_commission_rewards();
