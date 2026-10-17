ALTER TABLE company
    ADD COLUMN IF NOT EXISTS representative_national_card_key VARCHAR(255),
    ADD COLUMN IF NOT EXISTS introduction_letter_key          VARCHAR(255),
    ADD COLUMN IF NOT EXISTS articles_of_association_key      VARCHAR(255),
    ADD COLUMN IF NOT EXISTS official_newspaper_key           VARCHAR(255);

CREATE TABLE IF NOT EXISTS company_board_member_document
(
    id          SERIAL PRIMARY KEY,
    company_id  INTEGER      NOT NULL REFERENCES company (id),
    s3_key      VARCHAR(255) NOT NULL,
    create_date TIMESTAMP    NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_company_board_member_document_company_id
    ON company_board_member_document (company_id);
