INSERT INTO configs(name, referral_commission_reward) VALUES ('default', 0.3) ON CONFLICT DO NOTHING;

INSERT INTO payment_status(status) VALUES ('pending'), ('checked_out') ON CONFLICT DO NOTHING;