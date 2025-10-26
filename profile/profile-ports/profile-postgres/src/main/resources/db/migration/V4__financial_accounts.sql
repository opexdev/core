drop function IF EXISTS triger_linked_account_function();
drop function IF EXISTS triger_delete_linked_account_function();

DROP TABLE IF EXISTS linked_bank_account_history;
DROP TABLE IF EXISTS linked_bank_account;

CREATE TABLE IF NOT EXISTS bank_account
(
    id             SERIAL PRIMARY KEY,
    uuid           VARCHAR(36) NOT NULL,
    name           VARCHAR(100),
    card_number    VARCHAR(16),
    iban           VARCHAR(26),
    account_number VARCHAR(24),
    bank           VARCHAR(20),
    status         VARCHAR(20) NOT NULL,
    create_date    TIMESTAMP   NOT NULL,
    update_date    TIMESTAMP,
    creator        VARCHAR(100),
    CHECK (card_number IS NOT NULL OR iban IS NOT NULL),
    UNIQUE (uuid, card_number, iban, account_number, status)
);
CREATE INDEX idx_bank_account_uuid ON bank_account (uuid);

CREATE TABLE IF NOT EXISTS address_book
(
    id           SERIAL PRIMARY KEY,
    uuid         VARCHAR(36)  NOT NULL,
    address      VARCHAR(72)  NOT NULL,
    name         VARCHAR(100) NOT NULL,
    address_type VARCHAR(20)  NOT NULL,
    create_date  TIMESTAMP    NOT NULL,
    update_date  TIMESTAMP,
    UNIQUE (uuid, address, address_type)
);
CREATE INDEX idx_address_book_uuid ON address_book (uuid);
