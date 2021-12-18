CREATE TABLE IF NOT EXISTS configs (
    name VARCHAR(72) PRIMARY KEY,
    referral_commission_reward DECIMAL NOT NULL
);

CREATE TABLE IF NOT EXISTS referral_codes (
    id SERIAL PRIMARY KEY,
    uuid VARCHAR(72) NOT NULL,
    code VARCHAR(255) NOT NULL UNIQUE,
    referrer_commission DECIMAL NOT NULL,
    referent_commission DECIMAL NOT NULL
);

CREATE TABLE IF NOT EXISTS references (
    id SERIAL PRIMARY KEY,
    uuid VARCHAR(72) NOT NULL UNIQUE,
    referral_code_id INTEGER REFERENCES referral_codes(id),
    UNIQUE(uuid, referral_code_id)
);

CREATE TABLE IF NOT EXISTS commission_rewards (
    id SERIAL PRIMARY KEY,
    referrerUuid VARCHAR(72) NOT NULL,
    referentUuid VARCHAR(72) NOT NULL,
    referralCode VARCHAR(72) NOT NULL REFERENCES referral_codes(code),
    richTradeId INTEGER NOT NULL,
    referrerShare DECIMAL NOT NULL,
    referentShare DECIMAL NOT NULL
);
