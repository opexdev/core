CREATE TABLE IF NOT EXISTS currency_localization
(
    id                SERIAL PRIMARY KEY,
    currency          VARCHAR(25) NOT NULL REFERENCES currency (symbol) ON DELETE CASCADE,
    name              VARCHAR(25),
    title             VARCHAR(25),
    alias             VARCHAR(25),
    description       TEXT,
    short_description TEXT,
    language          VARCHAR(10) NOT NULL,
    UNIQUE (currency, language)
);

INSERT INTO currency_localization (currency,
                                   name,
                                   title,
                                   alias,
                                   description,
                                   short_description,
                                   language)
SELECT symbol, name, title, alias, description, short_description, 'EN'
FROM currency;

ALTER TABLE currency
    DROP COLUMN name,
    DROP COLUMN title,
    DROP COLUMN alias,
    DROP COLUMN description,
    DROP COLUMN short_description;