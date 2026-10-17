ALTER TABLE profile
    ADD COLUMN IF NOT EXISTS person_type VARCHAR(10) NOT NULL DEFAULT 'NATURAL';

CREATE TABLE IF NOT EXISTS company
(
    id                  SERIAL PRIMARY KEY,
    registration_number VARCHAR(50) NOT NULL UNIQUE,
    national_id         VARCHAR(20) NOT NULL UNIQUE,
    create_date         TIMESTAMP   NOT NULL,
    update_date         TIMESTAMP,
    creator             VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS company_representative
(
    id          SERIAL PRIMARY KEY,
    company_id  INTEGER     NOT NULL REFERENCES company (id),
    uuid        VARCHAR(36) NOT NULL UNIQUE,
    create_date TIMESTAMP   NOT NULL,
    creator     VARCHAR(100)
);
CREATE INDEX IF NOT EXISTS idx_company_representative_company_id ON company_representative (company_id);
