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

CREATE TABLE IF NOT EXISTS "references" (
    id SERIAL PRIMARY KEY,
    uuid VARCHAR(72) NOT NULL UNIQUE,
    referral_code_id INTEGER NOT NULL REFERENCES referral_codes(id),
    UNIQUE(uuid, referral_code_id)
);

CREATE TABLE IF NOT EXISTS commission_rewards (
    id SERIAL PRIMARY KEY,
    referrer_uuid VARCHAR(72) NOT NULL,
    referent_uuid VARCHAR(72) NOT NULL,
    referral_code VARCHAR(72) NOT NULL REFERENCES referral_codes(code),
    rich_trade_id INTEGER NOT NULL,
    referrer_share DECIMAL NOT NULL,
    referent_share DECIMAL NOT NULL
);
