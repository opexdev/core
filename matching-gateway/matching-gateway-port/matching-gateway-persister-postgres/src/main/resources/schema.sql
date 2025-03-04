CREATE TABLE IF NOT EXISTS PAIR_SETTING
(
    pair             VARCHAR(72) PRIMARY KEY,
    is_available        BOOLEAN     NOT NULL DEFAULT TRUE,
    update_date      TIMESTAMP
    );
COMMIT;
