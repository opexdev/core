INSERT INTO
   symbol_maps(symbol, value)
VALUES
   ('btc_usdt', 'BTCUSDT') ON CONFLICT DO NOTHING;

INSERT INTO
   symbol_maps(symbol, value)
VALUES
   ('eth_usdt', 'ETHUSDT') ON CONFLICT DO NOTHING;

INSERT INTO
   symbol_maps(symbol, value)
VALUES
   ('eth_btc', 'ETHBTC') ON CONFLICT DO NOTHING;

INSERT INTO
   symbol_maps(symbol, value)
VALUES
   ('nln_usdt', 'NLNUSDT') ON CONFLICT DO NOTHING;

INSERT INTO
   symbol_maps(symbol, value)
VALUES
   ('nln_btc', 'NLNBTC') ON CONFLICT DO NOTHING;
