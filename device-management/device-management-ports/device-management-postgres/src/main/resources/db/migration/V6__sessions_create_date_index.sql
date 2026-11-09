CREATE INDEX IF NOT EXISTS idx_sessions_create_date
    ON sessions (create_date);
CREATE INDEX IF NOT EXISTS idx_user_devices_last_login_date
    ON user_devices (last_login_date);