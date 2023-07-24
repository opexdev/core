CREATE TABLE IF NOT EXISTS profile
(
    id                SERIAL PRIMARY KEY,
    email             VARCHAR(100) NOT NULL UNIQUE,
    last_name          VARCHAR(256) ,
    user_id           VARCHAR(100) NOT NULL UNIQUE,
    create_date       TIMESTAMP,
    identifier        VARCHAR(100),
    address           VARCHAR(256),
    first_name        VARCHAR(256),
    telephone         VARCHAR(256),
    mobile            VARCHAR(256),
    nationality       VARCHAR(256),
    gender            BOOLEAN,
    birth_date        TIMESTAMP,
    status            VARCHAR(100),
    postal_code       VARCHAR(100),
    creator           VARCHAR(100),
    last_update_date  TIMESTAMP DEFAULT CURRENT_DATE
 );

ALTER TABLE profile ADD COLUMN creator VARCHAR(100);
CREATE TABLE IF NOT EXISTS profile_history
(
    id                SERIAL PRIMARY KEY,
    email             VARCHAR(100) NOT NULL ,
    last_name            VARCHAR(256) ,
    user_id           VARCHAR(100) NOT NULL ,
    create_date       TIMESTAMP,
    identifier        VARCHAR(100),
    address           VARCHAR(256),
    first_name        VARCHAR(256),
    telephone         VARCHAR(256),
    mobile            VARCHAR(256),
    nationality       VARCHAR(256),
    gender            BOOLEAN,
    birth_date        TIMESTAMP,
    status            VARCHAR(100),
    last_update_date  TIMESTAMP,
    original_data_id  VARCHAR(100) NOT NULL,
    creator           VARCHAR(100),
    issuer            VARCHAR(100) ,
    postal_code            VARCHAR(100),
    change_request_date TIMESTAMP,
    change_request_type VARCHAR(100)
    );


ALTER TABLE profile_history ADD COLUMN creator VARCHAR(100);


CREATE TABLE IF NOT EXISTS revoke_permission
(
    id                SERIAL PRIMARY KEY,
    user_id           VARCHAR(100) NOT NULL,
    action_type       VARCHAR(100),
    create_date       TIMESTAMP,
    exp_time          VARCHAR(100),
    detail            VARCHAR(100),
    enable            BOOLEAN DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS revoke_permission_history
(
    id                SERIAL PRIMARY KEY,
    user_id           VARCHAR(100) NOT NULL,
    action_type       VARCHAR(100),
    create_date       TIMESTAMP,
    exp_time          VARCHAR(100),
    detail            VARCHAR(100),
    enable            BOOLEAN DEFAULT TRUE,
    original_data_id  VARCHAR(100) NOT NULL,
    issuer            VARCHAR(100) ,
    change_request_date TIMESTAMP,
    change_request_type VARCHAR(100)
    );





DROP TRIGGER IF EXISTS profile_log_update on public.profile;
DROP TRIGGER IF EXISTS profile_log_delete on public.profile;


CREATE OR REPLACE FUNCTION triger_function() RETURNS TRIGGER AS
$BODY$
BEGIN
INSERT INTO public.profile_history (original_data_id,change_request_date,change_request_type,email,user_id,create_date,identifier,address,first_name,last_name,mobile,telephone,nationality,gender,birth_date,status,postal_code,creator)
VALUES(OLD.id,now(),'UPDATE',OLD.email,OLD.user_id,OLD.create_date,OLD.identifier,OLD.address,OLD.first_name,OLD.last_name,OLD.mobile,OLD.telephone,OLD.nationality,OLD.gender,OLD.birth_date,OLD.status,OLD.postal_code,OLD.creator);
RETURN NULL;
END;
$BODY$
language plpgsql;




CREATE OR REPLACE FUNCTION triger_delete_function() RETURNS TRIGGER AS
$BODY$
BEGIN
INSERT INTO public.profile_history (original_data_id,change_request_date,change_request_type,email,user_id,create_date,identifier,address,first_name,last_name,mobile,telephone,nationality,gender,birth_date,status,postal_code,creator)
VALUES(OLD.id,now(),'DELETE',OLD.email,OLD.user_id,OLD.create_date,OLD.identifier,OLD.address,OLD.first_name,OLD.last_name,OLD.mobile,OLD.telephone,OLD.nationality,OLD.gender,OLD.birth_date,OLD.status,OLD.postal_code,OLD.creator);
RETURN NULL;
END;
$BODY$
language plpgsql;


CREATE TRIGGER  profile_log_update  AFTER UPDATE ON profile FOR EACH ROW   EXECUTE PROCEDURE triger_function();
-- #INSERT INTO profile_history(original_data_id,change_request_date,change_request_type,email,user_id,create_date,identifier,address,name,last_name,mobile,telephone,nationality,gender,birth_date,status)  values(OLD.id,now(),'UPDATE',OLD.email,OLD.user_id,OLD.create_date,OLD.identifier,OLD.address,OLD.first_name,OLD.last_name,OLD.mobile,OLD.telephone,OLD.nationality,OLD.gender,OLD.birth_date,OLD.status)  ;
-- #CREATE TRIGGER profile_log_insert  AFTER INSERT ON profile  FOR EACH ROW   INSERT INTO profile_history(original_data_id,change_request_date,change_request_type,email,user_id,create_date,identifier,address,name,last_name,mobile,telephone,nationality,gender,birth_date,status)  values(NEW.id,now(),'INSERT',NEW.email,NEW.user_id,NEW.create_date,NEW.identifier,NEW.address,NEW.first_name,NEW.last_name,NEW.mobile,NEW.telephone,NEW.nationality,NEW.gender,NEW.birth_date,NEW.status)  ;
CREATE TRIGGER  profile_log_delete  AFTER DELETE ON profile  FOR EACH ROW  EXECUTE PROCEDURE triger_delete_function() ;
-- INSERT INTO profile_history(original_data_id,change_request_date,change_request_type,email,user_id,create_date,identifier,address,name,last_name,mobile,telephone,nationality,gender,birth_date,status)  values(OLD.id,now(),'DELETE',OLD.email,OLD.user_id,OLD.create_date,OLD.identifier,OLD.address,OLD.first_name,OLD.last_name,OLD.mobile,OLD.telephone,OLD.nationality,OLD.gender,OLD.birth_date,OLD.status)  ;


