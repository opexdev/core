CREATE TABLE test_db_2
(
    id            SERIAL PRIMARY KEY,
    address_type  VARCHAR(20) NOT NULL,
    address_regex VARCHAR(72) NOT NULL,
    memo_regex    VARCHAR(72)
);