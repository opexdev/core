CREATE TABLE IF NOT EXISTS configs (
    name VARCHAR(72) PRIMARY KEY,
    referral_commission_reward DECIMAL NOT NULL
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
    referrer_uuid VARCHAR(72) NOT NULL,
    referent_uuid VARCHAR(72) NOT NULL,
    referral_code VARCHAR(72) NOT NULL REFERENCES referral_codes(code),
    rich_trade_id INTEGER NOT NULL,
    referent_order_side VARCHAR(20) NOT NULL,
    referrer_share DECIMAL NOT NULL,
    referent_share DECIMAL NOT NULL,
    UNIQUE(rich_trade_id, referrer_uuid, referent_uuid, referent_order_side)
);

CREATE TABLE IF NOT EXISTS payment_records (
    id BIGSERIAL PRIMARY KEY,
    commission_rewards_id BIGINTEGER NOT NULL REFERENCES commission_rewards(id),
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_status VARCHAR(20) NOT NULL REFERENCES payment_status(status) DEFAULT 'pending'
);

CREATE INDEX IF NOT EXISTS payment_status_index ON payment_records(status);
