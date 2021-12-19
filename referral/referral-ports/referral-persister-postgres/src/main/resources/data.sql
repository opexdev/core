INSERT INTO
   configs(name, referral_commission_reward)
VALUES
   ('default', 0.3) ON CONFLICT DO NOTHING;