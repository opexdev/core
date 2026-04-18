CREATE TABLE IF NOT EXISTS terminal_localization
(
    id          SERIAL PRIMARY KEY,
    terminal_id BIGINT      NOT NULL REFERENCES terminal (id) ON DELETE CASCADE,
    description TEXT        NOT NULL,
    language    VARCHAR(10) NOT NULL,
    UNIQUE (terminal_id, language)
);

ALTER TABLE terminal
    DROP COLUMN description;