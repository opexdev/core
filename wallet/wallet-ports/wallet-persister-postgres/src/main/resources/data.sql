INSERT INTO wallet_owner(id, uuid, title, level)
VALUES (1, '1', 'system', 'basic')
ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('wallet_owner', 'id'), (SELECT MAX(id) FROM wallet_owner));

INSERT INTO currency(name, symbol, precision)
VALUES ('btc', 'btc', 0.000001),
       ('eth', 'eth', 0.00001),
       ('usdt', 'usdt', 0.01),
       ('nln', 'nln', 1),
       ('IRT', 'IRT', 1)
ON CONFLICT DO NOTHING;

-- Test currency
INSERT INTO currency(name, symbol, precision)
VALUES ('tbtc', 'tbtc', 0.000001),
       ('teth', 'teth', 0.00001),
       ('tusdt', 'tusdt', 0.01),
       ('nln', 'nln', 1)
ON CONFLICT DO NOTHING;

INSERT INTO currency_rate(id, source_currency, dest_currency, rate)
VALUES (1, 'btc', 'nln', 5500000),
       (2, 'usdt', 'nln', 100),
       (3, 'btc', 'usdt', 55000),
       (4, 'eth', 'usdt', 3800)
ON CONFLICT DO NOTHING;

-- Test currency rate
INSERT INTO currency_rate(id, source_currency, dest_currency, rate)
VALUES (5, 'tbtc', 'nln', 5500000),
       (6, 'tusdt', 'nln', 100),
       (7, 'tbtc', 'tusdt', 55000),
       (8, 'teth', 'tusdt', 3800)
ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('currency_rate', 'id'), (SELECT MAX(id) FROM currency_rate));

INSERT INTO wallet(id, owner, wallet_type, currency, balance)
VALUES (1, 1, 'main', 'btc', 10),
       (2, 1, 'exchange', 'btc', 0),
       (3, 1, 'main', 'usdt', 550000),
       (4, 1, 'exchange', 'usdt', 0),
       (5, 1, 'main', 'nln', 100000000),
       (6, 1, 'exchange', 'nln', 0),
       (7, 1, 'main', 'eth', 10000),
       (8, 1, 'exchange', 'eth', 0),
       (9, 1, 'main', 'IRT', 100000000),
       (10, 1, 'exchange', 'IRT', 0)
ON CONFLICT DO NOTHING;

-- Test wallet
INSERT INTO wallet(id, owner, wallet_type, currency, balance)
VALUES (11, 1, 'main', 'tbtc', 10),
       (12, 1, 'exchange', 'tbtc', 0),
       (13, 1, 'main', 'tusdt', 550000),
       (14, 1, 'exchange', 'tusdt', 0),
       (15, 1, 'main', 'nln', 100000000),
       (16, 1, 'exchange', 'nln', 0),
       (17, 1, 'main', 'teth', 10000),
       (18, 1, 'exchange', 'teth', 0)
ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('wallet', 'id'), (SELECT MAX(id) FROM wallet));

INSERT INTO user_limits(id,
                        level,
                        owner,
                        action,
                        wallet_type,
                        daily_total,
                        daily_count,
                        monthly_total,
                        monthly_count)
VALUES (1, null, 1, 'withdraw', 'main', 1000, 100, 10000, 1000)
ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('user_limits', 'id'), (SELECT MAX(id) FROM user_limits));
