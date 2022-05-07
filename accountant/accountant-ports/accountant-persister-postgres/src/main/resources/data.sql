INSERT INTO pair_config
VALUES ('btc_usdt', 'btc', 'usdt', 0.000001, 0.01, 55000),
       ('eth_usdt', 'eth', 'usdt', 0.00001, 0.01, 3800),
       ('btc_irt', 'btc', 'irt', 0.000001, 0.01, 55000),
       ('eth_irt', 'eth', 'irt', 0.00001, 0.01, 3800),
       ('nln_usdt', 'nln', 'usdt', 1.0, 0.01, 0.01),
       ('nln_btc', 'nln', 'btc', 1.0, 0.000001, 1 / 5500000),
       ('nln_eth', 'nln', 'eth', 1.0, 0.000001, 1 / 550000)
ON CONFLICT DO NOTHING;

-- Test pairs
INSERT INTO pair_config
VALUES ('tbtc_tusdt', 'tbtc', 'tusdt', 0.000001, 0.01, 55000),
       ('teth_tusdt', 'teth', 'tusdt', 0.00001, 0.01, 3800),
       ('nln_tusdt', 'nln', 'tusdt', 1.0, 0.01, 0.01),
       ('nln_tbtc', 'nln', 'tbtc', 1.0, 0.000001, 1 / 5500000),
       ('nln_teth', 'nln', 'teth', 1.0, 0.000001, 1 / 550000)
ON CONFLICT DO NOTHING;

INSERT INTO pair_fee_config
VALUES (1, 'btc_usdt', 'ASK', '*', 0.01, 0.01),
       (2, 'btc_usdt', 'BID', '*', 0.01, 0.01),
       (3, 'nln_usdt', 'ASK', '*', 0.01, 0.01),
       (4, 'nln_usdt', 'BID', '*', 0.01, 0.01),
       (5, 'nln_btc', 'ASK', '*', 0.01, 0.01),
       (6, 'nln_btc', 'BID', '*', 0.01, 0.01),
       (7, 'eth_usdt', 'ASK', '*', 0.01, 0.01),
       (8, 'eth_usdt', 'BID', '*', 0.01, 0.01),
       (9, 'nln_eth', 'ASK', '*', 0.01, 0.01),
       (10, 'nln_eth', 'BID', '*', 0.01, 0.01),
       (11, 'btc_irt', 'ASK', '*', 0.01, 0.01),
       (12, 'btc_irt', 'BID', '*', 0.01, 0.01),
       (13, 'eth_irt', 'ASK', '*', 0.01, 0.01),
       (14, 'eth_irt', 'BID', '*', 0.01, 0.01)
ON CONFLICT DO NOTHING;

-- Test pair configs
INSERT INTO pair_fee_config
VALUES (15, 'tbtc_tusdt', 'ASK', '*', 0.01, 0.01),
       (16, 'tbtc_tusdt', 'BID', '*', 0.01, 0.01),
       (17, 'nln_tusdt', 'ASK', '*', 0.01, 0.01),
       (18, 'nln_tusdt', 'BID', '*', 0.01, 0.01),
       (19, 'nln_tbtc', 'ASK', '*', 0.01, 0.01),
       (20, 'nln_tbtc', 'BID', '*', 0.01, 0.01),
       (21, 'teth_tusdt', 'ASK', '*', 0.01, 0.01),
       (22, 'teth_tusdt', 'BID', '*', 0.01, 0.01),
       (23, 'nln_teth', 'ASK', '*', 0.01, 0.01),
       (24, 'nln_teth', 'BID', '*', 0.01, 0.01)
ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('pair_fee_config', 'id'), (SELECT MAX(id) FROM pair_fee_config));

COMMIT;
