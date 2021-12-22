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

CREATE TABLE IF NOT EXISTS commission_rewards (
    id BIGSERIAL PRIMARY KEY,
    referrer_uuid VARCHAR(72) NOT NULL,
    referent_uuid VARCHAR(72) NOT NULL,
    referral_code VARCHAR(72) NOT NULL REFERENCES referral_codes(code),
    rich_trade_id INTEGER NOT NULL,
    referrer_share DECIMAL NOT NULL,
    referent_share DECIMAL NOT NULL,
    checkout_date DATE,
    is_checked_out BOOLEAN NOT NULL GENERATED ALWAYS AS (checkout_date IS NOT NULL) STORED
);

CREATE INDEX IF NOT EXISTS is_checked_index ON commission_rewards(is_checked_out);
