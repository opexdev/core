CREATE TABLE IF NOT EXISTS configs (
    name VARCHAR(72) PRIMARY KEY,
    referral_commission_reward DECIMAL NOT NULL
)

CREATE TABLE IF NOT EXISTS referrals (
    id SERIAL PRIMARY KEY,
    uuid VARCHAR(72),
    code VARCHAR(255) NOT NULL UNIQUE,
    parent INTEGER REFERENCES referrals(id),
    referrer_commission DECIMAL NOT NULL,
    referent_commission DECIMAL NOT NULL
)