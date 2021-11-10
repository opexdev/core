INSERT INTO
   wallet_owner(id, uuid, title, level)
VALUES
   (1, '1', 'system', 'basic') ON CONFLICT DO NOTHING;

INSERT INTO
   currency(name, symbol, precision)
VALUES
   ('btc', 'btc', 0.000001),
   ('eth', 'eth', 0.00001)
   ('usdt', 'usdt', 0.01),
   ('nln', 'nln', 1) ON CONFLICT DO NOTHING;

INSERT INTO
   currency_rate(id, source_currency, dest_currency, rate)
VALUES
   (1, 'btc', 'nln', 5500000),
   (1, 'usdt', 'nln', 100),
   (1, 'btc', 'usdt', 55000),
   (1, 'eth', 'usdt', 3800) ON CONFLICT DO NOTHING;

INSERT INTO
   wallet(id, owner, wallet_type, currency, balance)
VALUES
   (1, 1, 'main', 'btc', 10),
   (2, 1, 'exchange', 'btc', 0),
   (3, 1, 'main', 'usdt', 550000),
   (4, 1, 'exchange', 'usdt', 0),
   (5, 1, 'main', 'nln', 100000000),
   (6, 1, 'exchange', 'nln', 0),
   (7, 1, 'main', 'eth', 10000),
   (8, 1, 'exchange', 'eth', 0) ON CONFLICT DO NOTHING;

INSERT INTO
   user_limits (
      id,
      level,
      owner,
      action,
      wallet_type,
      daily_total,
      daily_count,
      monthly_total,
      monthly_count
   )
VALUES
   (1, null, 1, 'withdraw', 'main', 1000, 100, 10000, 1000) ON CONFLICT DO NOTHING;
