INSERT INTO
   symbol_maps(symbol, value)
VALUES
   ('btc_usdt', 'BTCUSDT'),
   ('eth_usdt', 'ETHUSDT'),
   ('eth_btc', 'ETHBTC'),
   ('nln_usdt', 'NLNUSDT'),
   ('nln_btc', 'NLNBTC') ON CONFLICT DO NOTHING;
