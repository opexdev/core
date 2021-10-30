insert into currency values ('BTC', 'Bitcoin') ON CONFLICT DO NOTHING;
    insert into chains values ('Bit') ON CONFLICT DO NOTHING;
    insert into chains values ('Bsc') ON CONFLICT DO NOTHING;
    insert into address_types(id, address_type, address_regex) values (1, 'BTC', '.*') ON CONFLICT DO NOTHING;
    insert into address_types(id, address_type, address_regex) values (2, 'ETH', '.*') ON CONFLICT DO NOTHING;
    insert into chain_address_types (chain_name, addr_type_id) values ('Bit', 1) ON CONFLICT DO NOTHING;
    insert into chain_address_types (chain_name, addr_type_id) values ('Bsc', 2) ON CONFLICT DO NOTHING;
    insert into currency_implementations (
        id,
        symbol,
        chain,
        token,
        token_address,
        token_name,
        withdraw_enabled,
        withdraw_fee,
        withdraw_min
    )values(1, 'BTC', 'Bit', false, null, null, true, 0.0001, 0.0001)
    ,(2, 'BTC', 'Bsc', true, '0x1111', 'WBTC', true, 0.00001, 0.000001) ON CONFLICT DO NOTHING;
