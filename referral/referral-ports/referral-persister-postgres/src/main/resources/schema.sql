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

CREATE TABLE IF NOT EXISTS referents (
    id SERIAL PRIMARY KEY,
    uuid VARCHAR(72) NOT NULL UNIQUE,
    referral_code_id INTEGER REFERENCES referral_codes(id),
    UNIQUE(uuid, referral_code_id)
);
