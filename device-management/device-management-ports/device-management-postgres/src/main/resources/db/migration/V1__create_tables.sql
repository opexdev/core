CREATE TABLE IF NOT EXISTS devices
(
    id               BIGSERIAL PRIMARY KEY,
    device_uuid      VARCHAR(255) NOT NULL UNIQUE,
    os               VARCHAR(50),
    os_version       VARCHAR(50),
    app_version      VARCHAR(50),
    push_token       VARCHAR(255),
    create_date      TIMESTAMP    NOT NULL DEFAULT NOW(),
    last_update_date TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS user_devices
(
    id               BIGSERIAL PRIMARY KEY,
    uuid             VARCHAR(255) NOT NULL,
    device_id        BIGINT       NOT NULL,
    first_login_date TIMESTAMP    NOT NULL DEFAULT NOW(),
    last_login_date  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_device FOREIGN KEY (device_id) REFERENCES devices (id) ON DELETE CASCADE,
    CONSTRAINT uc_user_device UNIQUE (uuid, device_id)
);


CREATE TABLE IF NOT EXISTS sessions
(
    id            BIGSERIAL PRIMARY KEY,
    session_state VARCHAR(255) NOT NULL UNIQUE,
    uuid          VARCHAR(255) NOT NULL,
    device_id     BIGINT       NOT NULL,
    status        VARCHAR(15)  NOT NULL DEFAULT 'ACTIVE',
    create_date   TIMESTAMP    NOT NULL DEFAULT NOW(),
    expire_date   TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_session_device FOREIGN KEY (device_id) REFERENCES devices (id) ON DELETE CASCADE
);
