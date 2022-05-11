INSERT INTO wallet_owner(id, uuid, title, level)
VALUES (1, '1', 'system', 'basic')
ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('wallet_owner', 'id'), (SELECT MAX(id) FROM wallet_owner));
