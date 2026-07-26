ALTER TABLE terminal_localization
    ADD COLUMN owner VARCHAR(255);

UPDATE terminal_localization tl
SET owner = (
    SELECT t.owner
    FROM terminal t
    WHERE t.id = tl.terminal_id
);

ALTER TABLE terminal
    DROP COLUMN owner;