CREATE TABLE IF NOT EXISTS pair_setting
(
    pair         VARCHAR(72) PRIMARY KEY,
    is_available BOOLEAN NOT NULL DEFAULT FALSE,
    update_date  TIMESTAMP
);

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_name = 'pair_setting' AND column_name = 'min_order') THEN ALTER TABLE pair_setting
            ADD COLUMN min_order DECIMAL NOT NULL default 0.000001;
        END IF;
        IF NOT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_name = 'pair_setting' AND column_name = 'max_order') THEN ALTER TABLE pair_setting
            ADD COLUMN max_order DECIMAL NOT NULL default 100;
        END IF;
        IF NOT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_name = 'pair_setting' AND column_name = 'order_types') THEN ALTER TABLE pair_setting
            ADD COLUMN order_types varchar(255) NOT NULL default 'LIMIT,MARKET' ;
        END IF;
    END
$$;