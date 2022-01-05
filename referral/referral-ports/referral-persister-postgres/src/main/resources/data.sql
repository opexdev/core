INSERT INTO configs(name, referral_commission_reward, payment_currency, min_payment_amount) VALUES ('default', 0.3, 'usdt', 0) ON CONFLICT DO NOTHING;

INSERT INTO checkout_states(state) VALUES ('PENDING'), ('CHECKED_OUT') ON CONFLICT DO NOTHING;