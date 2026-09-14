-- The cross-rate sparkline query scans rate_history for rows whose base or quote side equals
-- the hub currency. These expression indexes let Postgres do a bitmap-OR of two index scans
-- instead of a full sequential scan + string parsing on every row.
CREATE INDEX IF NOT EXISTS idx_rate_history_quote_created_date
    ON rate_history (split_part(upper(symbol), '-', 2), created_date);

CREATE INDEX IF NOT EXISTS idx_rate_history_base_created_date
    ON rate_history (split_part(upper(symbol), '-', 1), created_date);
