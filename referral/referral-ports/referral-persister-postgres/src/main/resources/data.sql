INSERT INTO configs(name, referral_commission_reward, payment_asset_symbol, min_payment_amount) VALUES ('default', 0.3, 'usdt', 0) ON CONFLICT DO NOTHING;

INSERT INTO payment_status(status) VALUES ('pending'), ('checked_out') ON CONFLICT DO NOTHING;