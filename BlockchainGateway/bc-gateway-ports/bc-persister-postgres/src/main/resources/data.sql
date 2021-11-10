INSERT INTO
   currency
VALUES
   ('BTC', 'Bitcoin'), ('ETH', 'Ethereum') ON CONFLICT DO NOTHING;

INSERT INTO
   chains
VALUES
   ('bitcoin'), ('ethereum') ON CONFLICT DO NOTHING;

INSERT INTO
   address_types(id, address_type, address_regex)
VALUES
   (1, 'bitcoin', '.*'), (2, 'ethereum', '.*') ON CONFLICT DO NOTHING;

INSERT INTO
   chain_address_types(chain_name, addr_type_id)
VALUES
   ('bitcoin', 1), ('ethereum', 2) ON CONFLICT DO NOTHING;

INSERT INTO
   currency_implementations(
      id,
      symbol,
      chain,
      token,
      token_address,
      token_name,
      withdraw_enabled,
      withdraw_fee,
      withdraw_min,
      decimal
   )
VALUES
(
      1,
      'BTC',
      'bitcoin',
      false,
      null,
      null,
      true,
      0.0001,
      0.0001,
      0
   ),
(
      2,
      'ETH',
      'ethereum',
      true,
      null,
      null,
      true,
      0.00001,
      0.000001,
      18
   ) ON CONFLICT DO NOTHING;
