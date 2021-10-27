INSERT INTO
   PAIR_CONFIG
VALUES
   (
      'btc_usdt', 'btc', 'usdt', 0.000001, 0.01, 55000
   )
   ON CONFLICT DO NOTHING;
INSERT INTO
   PAIR_FEE_CONFIG
VALUES
   (
      1, 'btc_usdt', 'ASK', '*', 0.01, 0.01
   )
   ON CONFLICT DO NOTHING;
INSERT INTO
   PAIR_FEE_CONFIG
VALUES
   (
      2, 'btc_usdt', 'BID', '*', 0.01, 0.01
   )
   ON CONFLICT DO NOTHING;
INSERT INTO
   PAIR_CONFIG
VALUES
   (
      'eth_usdt', 'eth', 'usdt', 0.00001, 0.01, 3800
   )
   ON CONFLICT DO NOTHING;
INSERT INTO
   PAIR_FEE_CONFIG
VALUES
   (
      7, 'eth_usdt', 'ASK', '*', 0.01, 0.01
   )
   ON CONFLICT DO NOTHING;
INSERT INTO
   PAIR_FEE_CONFIG
VALUES
   (
      8, 'eth_usdt', 'BID', '*', 0.01, 0.01
   )
   ON CONFLICT DO NOTHING;
INSERT INTO
   PAIR_CONFIG
VALUES
   (
      'nln_usdt', 'nln', 'usdt', 1.0, 0.01, 0.01
   )
   ON CONFLICT DO NOTHING;
INSERT INTO
   PAIR_FEE_CONFIG
VALUES
   (
      3, 'nln_usdt', 'ASK', '*', 0.01, 0.01
   )
   ON CONFLICT DO NOTHING;
INSERT INTO
   PAIR_FEE_CONFIG
VALUES
   (
      4, 'nln_usdt', 'BID', '*', 0.01, 0.01
   )
   ON CONFLICT DO NOTHING;
INSERT INTO
   PAIR_CONFIG
VALUES
   (
      'nln_btc', 'nln', 'btc', 1.0, 0.000001, 1 / 5500000
   )
   ON CONFLICT DO NOTHING;
INSERT INTO
   PAIR_FEE_CONFIG
VALUES
   (
      5, 'nln_btc', 'ASK', '*', 0.01, 0.01
   )
   ON CONFLICT DO NOTHING;
INSERT INTO
   PAIR_FEE_CONFIG
VALUES
   (
      6, 'nln_btc', 'BID', '*', 0.01, 0.01
   )
   ON CONFLICT DO NOTHING;
COMMIT;
