CREATE TABLE IF NOT EXISTS user_status
(
    id
    SERIAL
    PRIMARY
    KEY,
    user_id
    VARCHAR
(
    100
) NOT NULL UNIQUE,
    kyc_level VARCHAR
(
    100
),
    reference_id VARCHAR
(
    100
),
    description VARCHAR
(
    256
),
    detail VARCHAR
(
    256
),
    last_update_date TIMESTAMP
    );


CREATE TABLE IF NOT EXISTS kyc_process
(
    id
    SERIAL
    PRIMARY
    KEY,
    user_id
    VARCHAR
(
    100
) NOT NULL,
    step VARCHAR
(
    100
),
    step_id VARCHAR
(
    100
),
    description VARCHAR
(
    256
),
    create_date TIMESTAMP,
    issuer VARCHAR
(
    100
),
    status VARCHAR
(
    100
),
    input VARCHAR
(
    256
),
    reference_id VARCHAR
(
    100
)
    );

CREATE TABLE IF NOT EXISTS user_status_history
(
    id
    SERIAL
    PRIMARY
    KEY,
    user_id
    VARCHAR
(
    100
) NOT NULL,
    kyc_level VARCHAR
(
    100
),
    reference_id VARCHAR
(
    100
),
    description VARCHAR
(
    256
),
    detail VARCHAR
(
    256
),
    last_update_date TIMESTAMP,
    change_issuer VARCHAR
(
    100
) ,
    change_request_date TIMESTAMP,
    change_request_type VARCHAR
(
    100
)
    );

DROP TRIGGER IF EXISTS user_level_log_update on public.user_status;
DROP TRIGGER IF EXISTS user_level_log_delete on public.user_status;


CREATE
OR REPLACE FUNCTION triger_function() RETURNS TRIGGER AS
$BODY$
BEGIN
INSERT INTO public.user_status_history (change_request_date, change_request_type, user_id, kyc_level, reference_id,
                                        description, detail, last_update_date)
VALUES (now(), 'UPDATE', OLD.user_id, OLD.kyc_level, OLD.reference_id, OLD.description, OLD.detail,
        OLD.last_update_date);
RETURN NULL;
END;
$BODY$
language plpgsql;


CREATE
OR REPLACE FUNCTION triger_delete_function() RETURNS TRIGGER AS
$BODY$
BEGIN
INSERT INTO public.user_status_history (change_request_date, change_request_type, user_id, kyc_level, reference_id,
                                        description, detail, last_update_date)
VALUES (now(), 'DELETE', OLD.user_id, OLD.kyc_level, OLD.reference_id, OLD.description, OLD.detail,
        OLD.last_update_date);
RETURN NULL;
END;
$BODY$
language plpgsql;

CREATE TRIGGER user_level_log_update
    AFTER UPDATE
    ON user_status
    FOR EACH ROW EXECUTE PROCEDURE triger_function();
CREATE TRIGGER user_level_log_delete
    AFTER DELETE
    ON user_status
    FOR EACH ROW EXECUTE PROCEDURE triger_delete_function();
