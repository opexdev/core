CREATE INDEX IF NOT EXISTS idx_rate_history_quote_created_date
    ON rate_history (split_part(upper(symbol), '-', 2), created_date);
CREATE INDEX IF NOT EXISTS idx_rate_history_base_created_date
    ON rate_history (split_part(upper(symbol), '-', 1), created_date);

