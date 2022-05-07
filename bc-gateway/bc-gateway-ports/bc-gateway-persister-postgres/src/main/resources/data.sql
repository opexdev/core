INSERT INTO currency
VALUES ('BTC', 'Bitcoin'),
       ('ETH', 'Ethereum'),
       ('USDT', 'Tether'),
       ('IRT', 'Toman'),
       ('BSC', 'Binance Smart Chain'),
       ('BUSD', 'Binance USD')
ON CONFLICT DO NOTHING;

-- Test currency
INSERT INTO currency
VALUES ('TBTC', 'Bitcoin (Test)'),
       ('TETH', 'Ethereum (Test)'),
       ('TUSDT', 'Tether (Test)')
ON CONFLICT DO NOTHING;

INSERT INTO chains
VALUES ('bitcoin'),
       ('ethereum'),
       ('bsc')
ON CONFLICT DO NOTHING;

-- Test chains
INSERT INTO chains
VALUES ('test-bitcoin'),
       ('test-ethereum')
ON CONFLICT DO NOTHING;

INSERT INTO address_types(id, address_type, address_regex)
VALUES (1, 'bitcoin', '.*'),
       (2, 'ethereum', '.*'),
       (3, 'test-bitcoin', '.*')
ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('address_types', 'id'), (SELECT MAX(id) FROM address_types));

INSERT INTO chain_address_types(chain_name, addr_type_id)
VALUES ('bitcoin', 1),
       ('ethereum', 2),
       ('bsc', 2)
ON CONFLICT DO NOTHING;

-- Test chain address types
INSERT INTO chain_address_types(chain_name, addr_type_id)
VALUES ('test-bitcoin', 3),
       ('test-ethereum', 2)
ON CONFLICT DO NOTHING;

INSERT INTO currency_implementations(id,
                                     symbol,
                                     chain,
                                     token,
                                     token_address,
                                     token_name,
                                     withdraw_enabled,
                                     withdraw_fee,
                                     withdraw_min,
                                     decimal)
VALUES (1, 'BTC', 'bitcoin', false, null, null, true, 0.0001, 0.0001, 0),
       (2, 'ETH', 'ethereum', false, null, null, true, 0.00001, 0.000001, 18),
       (3, 'USDT', 'ethereum', true, '0xdac17f958d2ee523a2206206994597c13d831ec7', 'USDT', true, 0.01, 0.01, 6),
       (4, 'BSC', 'bsc', false, null, null, true, 0.0001, 0.0001, 0),
       (5, 'BUSD', 'bsc', true, '0xe9e7cea3dedca5984780bafc599bd69add087d56', 'BUSD', true, 0.01, 0.01, 6)
ON CONFLICT DO NOTHING;

-- Test currency implementation
INSERT INTO currency_implementations(id,
                                     symbol,
                                     chain,
                                     token,
                                     token_address,
                                     token_name,
                                     withdraw_enabled,
                                     withdraw_fee,
                                     withdraw_min,
                                     decimal)
VALUES (6, 'TBTC', 'test-bitcoin', false, null, null, true, 0.0001, 0.0001, 0),
       (7, 'TETH', 'test-ethereum', false, null, null, true, 0.00001, 0.000001, 18),
       (8, 'TUSDT', 'test-ethereum', true, '0x110a13fc3efe6a245b50102d2d79b3e76125ae83', 'TUSDT', true, 0.01, 0.01, 6)
ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('currency_implementations', 'id'), (SELECT MAX(id) FROM currency_implementations));

INSERT INTO chain_endpoints(id, chain_name, endpoint_url)
VALUES (1, 'bitcoin', 'lb://chain-scan-gateway/bitcoin/transfers'),
       (2, 'ethereum', 'lb://chain-scan-gateway/eth/transfers'),
       (3, 'bsc', 'lb://chain-scan-gateway/bsc/transfers')
ON CONFLICT DO NOTHING;

-- Test chain endpoints
INSERT INTO chain_endpoints(id, chain_name, endpoint_url)
VALUES (4, 'test-bitcoin', 'lb://chain-scan-gateway/test-bitcoin/transfers'),
       (5, 'test-ethereum', 'lb://chain-scan-gateway/test-eth/transfers')
ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('chain_endpoints', 'id'), (SELECT MAX(id) FROM chain_endpoints));

INSERT INTO chain_sync_schedules
VALUES ('bitcoin', CURRENT_DATE, 600, 60),
       ('ethereum', CURRENT_DATE, 90, 60),
       ('bsc', CURRENT_DATE, 90, 60)
ON CONFLICT DO NOTHING;

-- Test chain scan schedules
INSERT INTO chain_sync_schedules
VALUES ('test-bitcoin', CURRENT_DATE, 600, 60),
       ('test-ethereum', CURRENT_DATE, 90, 60)
ON CONFLICT DO NOTHING;

INSERT INTO wallet_sync_schedules
VALUES (1, CURRENT_DATE, 10, 10000)
ON CONFLICT DO NOTHING;
