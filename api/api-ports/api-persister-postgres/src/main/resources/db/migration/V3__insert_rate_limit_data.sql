-- ________________ Admin Services ________________
INSERT INTO rate_limit_group (id, name, request_count, request_window_seconds, cooldown_seconds, max_penalty_level,
                              enabled)
VALUES (1, 'ADMIN', 15, 30, 120, 1, true);

INSERT INTO rate_limit_penalty (group_id, block_step, block_duration_seconds)
VALUES (1, 1, 60);

INSERT INTO rate_limit_endpoint (url, method, group_id, enabled, priority)
VALUES ('/opex/v1/admin/**', 'GET', 1, true, 1000);
INSERT INTO rate_limit_endpoint (url, method, group_id, enabled, priority)
VALUES ('/opex/v1/admin/**', 'POST', 1, true, 1000);
INSERT INTO rate_limit_endpoint (url, method, group_id, enabled, priority)
VALUES ('/opex/v1/admin/**', 'PUT', 1, true, 1000);
INSERT INTO rate_limit_endpoint (url, method, group_id, enabled, priority)
VALUES ('/opex/v1/admin/**', 'DELETE', 1, true, 1000);
-- __________________________________________________________________________________________________________________________

INSERT INTO rate_limit_group (id, name, request_count, request_window_seconds, cooldown_seconds, max_penalty_level,
                              enabled)
VALUES (2, 'HIGH_IMPACT', 5, 60, 600, 3, true);

INSERT INTO rate_limit_penalty (group_id, block_step, block_duration_seconds)
VALUES (2, 3, 300);
INSERT INTO rate_limit_penalty (group_id, block_step, block_duration_seconds)
VALUES (2, 2, 180);
INSERT INTO rate_limit_penalty (group_id, block_step, block_duration_seconds)
VALUES (2, 1, 120);


INSERT INTO rate_limit_group (id, name, request_count, request_window_seconds, cooldown_seconds, max_penalty_level,
                              enabled)
VALUES (3, 'LOW_IMPACT', 10, 60, 300, 3, true);

INSERT INTO rate_limit_penalty (group_id, block_step, block_duration_seconds)
VALUES (3, 3, 180);
INSERT INTO rate_limit_penalty (group_id, block_step, block_duration_seconds)
VALUES (3, 2, 120);
INSERT INTO rate_limit_penalty (group_id, block_step, block_duration_seconds)
VALUES (3, 1, 60);

INSERT INTO rate_limit_group (id, name, request_count, request_window_seconds, cooldown_seconds, max_penalty_level,
                              enabled)
VALUES (4, 'BOT', 100, 60, 120, 1, true);

INSERT INTO rate_limit_penalty (group_id, block_step, block_duration_seconds)
VALUES (4, 1, 60);


-- WithdrawController
INSERT INTO rate_limit_endpoint (url, method, group_id, enabled, priority)
VALUES ('/opex/v1/withdraw/**', 'POST', 2, true, 1);
INSERT INTO rate_limit_endpoint (url, method, group_id, enabled, priority)
VALUES ('/opex/v1/withdraw/**', 'PUT', 3, true, 1);
INSERT INTO rate_limit_endpoint (url, method, group_id, enabled, priority)
VALUES ('/opex/v1/withdraw/**', 'GET', 3, true, 1);

-- WalletController
INSERT INTO rate_limit_endpoint (url, method, group_id, enabled, priority)
VALUES ('/opex/v1/wallet/**', 'GET', 3, true, 1);
INSERT INTO rate_limit_endpoint (url, method, group_id, enabled, priority)
VALUES ('/opex/v1/wallet/deposit/address', 'GET', 2, true, 2);

-- VoucherController
INSERT INTO rate_limit_endpoint (url, method, group_id, enabled, priority)
VALUES ('/opex/v1/voucher/**', 'PUT', 2, true, 1);

-- UserHistoryController && UserDataController
INSERT INTO rate_limit_endpoint (url, method, group_id, enabled, priority)
VALUES ('/opex/v1/user/**', 'GET', 3, true, 1);
INSERT INTO rate_limit_endpoint (url, method, group_id, enabled, priority)
VALUES ('/opex/v1/user/**', 'POST', 3, true, 1);

-- OrderController
INSERT INTO rate_limit_endpoint (url, method, group_id, enabled, priority)
VALUES ('/opex/v1/order/**', 'GET', 3, true, 1);
INSERT INTO rate_limit_endpoint (url, method, group_id, enabled, priority)
VALUES ('/opex/v1/order/**', 'PUT', 3, true, 1);
INSERT INTO rate_limit_endpoint (url, method, group_id, enabled, priority)
VALUES ('/opex/v1/order/**', 'POST', 3, true, 1);

-- DepositController
INSERT INTO rate_limit_endpoint (url, method, group_id, enabled, priority)
VALUES ('/opex/v1/deposit/**', 'POST', 3, true, 1);

-- RateController
INSERT INTO rate_limit_endpoint (url, method, group_id, enabled, priority)
VALUES ('/opex/v1/otc/**', 'POST', 1, true, 1);
INSERT INTO rate_limit_endpoint (url, method, group_id, enabled, priority)
VALUES ('/opex/v1/otc/**', 'PUT', 4, true, 1);
INSERT INTO rate_limit_endpoint (url, method, group_id, enabled, priority)
VALUES ('/opex/v1/otc/**', 'DELETE', 1, true, 1);