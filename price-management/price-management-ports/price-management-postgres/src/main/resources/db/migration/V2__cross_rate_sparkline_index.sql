-- Case-insensitive symbol lookup for cross-rate queries.
CREATE INDEX IF NOT EXISTS idx_rate_history_upper_symbol_created_date
    ON rate_history (upper(symbol), created_date DESC);
