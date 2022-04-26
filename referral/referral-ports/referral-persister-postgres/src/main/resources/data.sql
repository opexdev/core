INSERT INTO configs(name, referral_commission_reward, payment_currency, min_payment_amount, payment_window_seconds, max_referral_code_per_user) VALUES ('default', 0.3, 'usdt', 0, 604800, 20) ON CONFLICT DO NOTHING;

INSERT INTO checkout_states(state) VALUES ('PENDING'), ('CHECKED_OUT') ON CONFLICT DO NOTHING;
