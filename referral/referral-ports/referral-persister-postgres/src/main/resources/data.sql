INSERT INTO
   configs(id, referral_commission_reward)
VALUES
   ('default', 0.3) ON CONFLICT DO NOTHING;