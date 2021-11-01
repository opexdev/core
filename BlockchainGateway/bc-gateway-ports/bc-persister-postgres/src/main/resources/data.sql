INSERT INTO
   currency
VALUES
   ('BTC', 'Bitcoin') ON CONFLICT DO NOTHING;

INSERT INTO
   chains
VALUES
   ('Bit') ON CONFLICT DO NOTHING;

INSERT INTO
   chains
VALUES
   ('Bsc') ON CONFLICT DO NOTHING;

INSERT INTO
   address_types(id, address_type, address_regex)
VALUES
   (1, 'BTC', '.*') ON CONFLICT DO NOTHING;

INSERT INTO
   address_types(id, address_type, address_regex)
VALUES
   (2, 'ETH', '.*') ON CONFLICT DO NOTHING;

INSERT INTO
   chain_address_types (chain_name, addr_type_id)
VALUES
   ('Bit', 1) ON CONFLICT DO NOTHING;

INSERT INTO
   chain_address_types (chain_name, addr_type_id)
VALUES
   ('Bsc', 2) ON CONFLICT DO NOTHING;

INSERT INTO
   currency_implementations (
      id,
      symbol,
      chain,
      token,
      token_address,
      token_name,
      withdraw_enabled,
      withdraw_fee,
      withdraw_min
   )
VALUES
(
      1,
      'BTC',
      'Bit',
      FALSE,
      NULL,
      NULL,
      TRUE,
      0.0001,
      0.0001
   ),
(
      2,
      'BTC',
      'Bsc',
      TRUE,
      '0x1111',
      'WBTC',
      TRUE,
      0.00001,
      0.000001
   ) ON CONFLICT DO NOTHING;
