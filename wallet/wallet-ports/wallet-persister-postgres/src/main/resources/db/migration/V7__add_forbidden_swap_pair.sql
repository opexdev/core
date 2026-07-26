CREATE TABLE IF NOT EXISTS forbidden_swap_pair
(
    id               SERIAL PRIMARY KEY,
    source_symbol    VARCHAR(25) NOT NULL REFERENCES currency (symbol),
    dest_symbol      VARCHAR(25) NOT NULL REFERENCES currency (symbol),
    last_update_date TIMESTAMP,
    create_date      TIMESTAMP
);