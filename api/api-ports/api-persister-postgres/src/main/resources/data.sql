INSERT INTO symbol_maps(symbol, value)
VALUES ('btc_usdt', 'BTCUSDT'),
       ('eth_usdt', 'ETHUSDT'),
       ('bnb_usdt', 'BSCUSDT'),
       ('eth_btc', 'ETHBTC'),
       ('nln_usdt', 'NLNUSDT'),
       ('nln_btc', 'NLNBTC'),
       ('btc_irt', 'BTCIRT'),
       ('eth_irt', 'ETHIRT'),
       ('bnb_irt', 'BSCIRT'),
       ('busd_irt', 'BUSDIRT')
ON CONFLICT DO NOTHING;

-- Test symbol mapper
INSERT INTO symbol_maps(symbol, value)
VALUES ('tbtc_tusdt', 'TBTCTUSDT'),
       ('teth_tusdt', 'TETHTUSDT'),
       ('teth_tbtc', 'TETHTBTC'),
       ('nln_tusdt', 'NLNTUSDT'),
       ('nln_tbtc', 'NLNTBTC')
ON CONFLICT DO NOTHING;
