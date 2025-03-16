create table if not exists otp
(
    id           serial primary key,
    code         varchar(16) not null,
    subject      text        not null,
    tracing_code text        not null unique,
    type         varchar(16) not null,
    expires_at   timestamp   not null,
    unique (subject, type)
);

create table if not exists otp_config
(
    type                varchar(16) primary key,
    expire_time_seconds integer not null default 60,
    char_count          integer not null default 6,
    is_enabled          boolean not null default true,
    check ( char_count between 4 and 100)
);