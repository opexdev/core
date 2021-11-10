INSERT INTO
   currency
VALUES
   ('BTC', 'Bitcoin'),
   ('ETH', 'Ethereum') ON CONFLICT DO NOTHING;

INSERT INTO
   chains
VALUES
   ('bitcoin'),
   ('ethereum') ON CONFLICT DO NOTHING;

INSERT INTO
   address_types(id, address_type, address_regex)
VALUES
   (1, 'bitcoin', '.*'),
   (2, 'ethereum', '.*') ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('address_types', 'id'), 2);

INSERT INTO
   chain_address_types(chain_name, addr_type_id)
VALUES
   ('bitcoin', 1),
   ('ethereum', 2) ON CONFLICT DO NOTHING;

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
   (1, 'BTC', 'bitcoin', false, null, null, true, 0.0001, 0.0001, 0),
   (2, 'ETH', 'ethereum', true, null, null, true, 0.00001, 0.000001, 18) ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('currency_implementations', 'id'), 2);

INSERT INTO
    reserved_addresses
VALUES
    (1, '0x9915b2B115C7061e19292FE6C6e63909448ACbdD', null, 2),
    (2, '0x35a55e21f7f62c5ed7d8ab0d6a1ca7d636aa4068', null, 2),
    (3, '0x31a45ea63421104e484436bda180ed396381d4c6', null, 2),
    (4, '0x726808b2cdc23ee50c698aea978e06c04be1be1d', null, 2),
    (5, '0x0b7543bf76f550d3e70fbac3f964d43fb6ce203a', null, 2),
    (6, '0x5fe284474b3b52f2fd693da5b5260a4572f90a56', null, 2),
    (7, '0x8593659960260335d82376a9fb4b88a3c49593ab', null, 2),
    (8, '0x597ea3bff601a9872978ee77d9e005427fc44396', null, 2),
    (9, '0x8c3e95eb46e25bd1fdefec295cbb424d20e93552', null, 2),
    (10, '0x5057f69595a93fd90be91478b0a6dfbf56df31c0', null, 2),
    (11, '0x974046c4ea7779e66b4e74581ed4e03565da2b44', null, 2),
    (12, '0xb1f81300b7d9e2f8df54bd8c369256e761a30ea5', null, 2),
    (13, '0xc486ba1a7a54c3d10fdbb049d50c9d7ba6e9d8f2', null, 2),
    (14, '0x23b23ee63de28993bdd80275465e82f580ba2458', null, 2),
    (15, '0x3b729f061057854198cced6c94a91bea92e76510', null, 2),
    (16, '0xf524e92481b91dd6a5db112241d93417ac9d7c41', null, 2),
    (17, '0xd7350aaf9bf051bc783cb77008d5dea5824ee3d1', null, 2),
    (18, '0xe5d9fb6a2881ad30f9911acd3c4afda94bc7261e', null, 2),
    (19, '0x7fc8630872e0fd392767008b91bfe3a5fe623ef9', null, 2),
    (20, '0xb254ece0214fbef616bc84855261bc9e54fe23e7', null, 2),
    (21, '0x9d2f4f5229d4bdb27c6e191198b500d827b80d31', null, 2),
    (22, '0xe50a8ebc5ced25289e0f4efaa934597b4c56336e', null, 2),
    (23, '0xa3bf7214bf5ab7a0dabb1a1ea78d6ae4a4270c6f', null, 2),
    (24, '0x18fdff00ead80e43224e59a85a2bc4c6259a88a3', null, 2),
    (25, '0x7200e5bbc596f4f6f4e29e9ba6954a11e40954cd', null, 2),
    (26, '0xcf6ee0077cf538e97b29b0667e96ff720dcacef3', null, 2),
    (27, '0xaa37606c560f2af28c01f3de838a771f262bc2cc', null, 2),
    (28, '0x5a664d1369e7eb5096ad57cb53c58047aad6f376', null, 2),
    (29, '0x3e53908e0f035ed1d471b61b5cc4074143dcefab', null, 2),
    (30, '0x50e25d825dc955783867d3d3510d20dcac4f905f', null, 2),
    (31, '0xae826d35823686e2c3a6cddd446c43da16ff3070', null, 2),
    (32, '0xdf934cf7f6455a5620f9de737daa82d4f5bf8f5b', null, 2),
    (33, '0x19669c309ccaa6b1afed0701b8e94ae7e6ecd3e1', null, 2),
    (34, '0x1506495ae195c96f5f56b79b4baa56a219189538', null, 2),
    (35, '0x855fb40e11de5a27061a1f6dc8cc18e4524fa8bf', null, 2),
    (36, '0x62e2b44b1e5a2ba4ffe2b4f9e5d067ef5d85f565', null, 2),
    (37, '0xe384f167cf7f59449a5b85160b1e9353da1d5470', null, 2),
    (38, '0xa51a98ed3ee695a0921767c920a71fbb371c572c', null, 2),
    (39, '0x8e6a769df4ececdcc26ce55265ce118f9edb5fa2', null, 2),
    (40, '0xde7cce5eb81a961aa7d789a719eb0a56cbd8c218', null, 2),
    (41, '0x20832a4bebe768eef9cbda0789b298d29ec11084', null, 2),
    (42, '0x32139bcb3a20adbfd60b8f69c06cf0e15304ecdc', null, 2),
    (43, '0x3d593222db1e9b18ba9fdd70f7bd380850dd5a3c', null, 2),
    (44, '0x3b99f972764765ac85bc65dae7f96206c0b20a02', null, 2),
    (45, '0xe4134fa19f3b798b44de09783b2004f6e1143717', null, 2),
    (46, '0xc76978e7d6fba69b2f980b36d24b53790fec6dc4', null, 2),
    (47, '0x8668bcfc22741d84241da064204d10b3a2616503', null, 2),
    (48, '0xf9510e1846206e18484742cb1a6c99c0e3fe745f', null, 2),
    (49, '0x5d5581d06f762ec296338cbea4d56b0eba8f7c03', null, 2),
    (50, '0x2d84a57ee481c9c7c1caf5dc151fa6205f9af319', null, 2),
    (51, '0xcc13def9292bcc6f41bd585c06325343c4eae0da', null, 2),
    (52, '0xa082667282fa333612cea74ccc69c70716dc3a86', null, 2),
    (53, '0xb640f1ac183453dc12c4bcbe446ecd7016881cdb', null, 2),
    (54, '0x68eacdc4f1ea5e722f69b05e736c6fab8163f37a', null, 2),
    (55, '0x142b062e66577c16f5e3959b4195cae78e7bec5a', null, 2),
    (56, '0x8999dd812ae8ad2b2cf70d46a6acbb59422bc531', null, 2),
    (57, '0x1a5fc4a430f40dcff19729cb80f4bb90d5286e88', null, 2),
    (58, '0xa36620eb16d699ea2d1e606d2cabaf67b41eba5b', null, 2),
    (59, '0xb626afc6f9dee216af119f02fed5d774b185ed1c', null, 2),
    (60, '0x3d3f936b6eca5670591609bee2cbe808ef789e7c', null, 2),
    (61, '0xcc12704fd2570ef08dc5ed3a4f5147da08f2866f', null, 2),
    (62, '0xf8b87807b34e4536bbf7a68653b06175a1673646', null, 2),
    (63, '0x03a49b796b3a0cca0de7a0e027895af0eb2103f0', null, 2),
    (64, '0xddabe826e1e2e7c0b9c1a4c61f3b3a45630595f7', null, 2),
    (65, '0x1782c18709139b16d91143f2f5f9b65f39e9165c', null, 2),
    (66, '0x40bae95ec1c3df040abb4d12b6536807ae04d46b', null, 2),
    (67, '0xac6bfcc5fe5c4c6bdaff2e7d71f013809896f98b', null, 2),
    (68, '0xdeee2d05ac3566efac90b7ee5403ba7e5332c2fe', null, 2),
    (69, '0xfe53059bf5cbf35a18f8e5705bd870e5e92b3387', null, 2),
    (70, '0x335815f17fc5cba2979c3f135ffb1dcdc2e94228', null, 2),
    (71, '0xa62cecf4650bd713c23f15b500fdd0933c9f8aac', null, 2),
    (72, '0xa954078b626c49ba1b17f1bff7f5bb0954792758', null, 2),
    (73, '0x997260c29aa2eddf3304ed5c0e440ac8e5cacde7', null, 2),
    (74, '0x046e80bd6af93389cdc2bed2336f464b3da7c0b7', null, 2),
    (75, '0xbb126abc010a53e5b0fd090a90a10cb4003720b5', null, 2),
    (76, '0xe720f50c1d16d574946d4090e0510f529a3c9bc5', null, 2),
    (77, '0x456cf66467a2f4713356c0a496c263d9e74a8105', null, 2),
    (78, '0x29ef6b2b52778cb7792bd0e5d7680b4982c020fc', null, 2),
    (79, '0x0cbd9032f92673ccc33ccf7da0a291a267380332', null, 2),
    (80, '0x65e9e60636aa7bc000332714e3d67ebe239ad110', null, 2),
    (81, '0x211c89ae3077c2ec7ed0fe4c0948f807713ffc56', null, 2),
    (82, '0x5efbcc7920453b9ecdc4983ea55d2d25d9ee6b37', null, 2),
    (83, '0xc2634f656a99c2b73a96a86a501f999a9cf02566', null, 2),
    (84, '0x55e1dbe468ec30e6736313bdbb3a36d1674c1989', null, 2),
    (85, '0x6bbf7d9a0dd3eea7458ea0adc70ff4d902091647', null, 2),
    (86, '0x549288e51f3d8723c2e7fb43dc0f92ec146a0ee8', null, 2),
    (87, '0x21b01a19c0357e4c70eedf82b8f76c31e3f6f325', null, 2),
    (88, '0xf56ba97281ff966c30b9a66bd6a4c7a50c286020', null, 2),
    (89, '0x0a4b3b72ef9a66b10d6e93e894de1c0fb0bb8eb6', null, 2),
    (90, '0xced6c8176da9bba6d6811c55723b1cfbde02fdf1', null, 2),
    (91, '0x73657fb131852187d8f2068616de14f449dd5c4b', null, 2),
    (92, '0xed8070518c219ddf89b2bbffd7c5d6cd72df1470', null, 2),
    (93, '0x8efddeaca85c72580f54aaa00df8f8ae59ce5633', null, 2),
    (94, '0x2b6b34f70a3c762e406aa1d55902665747c3b7e0', null, 2),
    (95, '0x533bcb1fa7eedc83bd11771c60aca12632899af1', null, 2),
    (96, '0xd5d79c3410c6bc6ae1c27329c93e0a943e7efc72', null, 2),
    (97, '0x2307fdc11547091b67b36508b680399da508825a', null, 2),
    (98, '0x2858f7b3e21f08dce6530ecb68dd3c1756fcba01', null, 2),
    (99, '0xa2edce267a43e1cf3b58d62f5da75f4ffcec85df', null, 2),
    (100, '0xf6e1ec3f817c8b76b860588dad74354e1772c98c', null, 2) ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('reserved_addresses', 'id'), 100);
