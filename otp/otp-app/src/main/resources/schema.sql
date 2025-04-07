create table if not exists otp
(
    id           serial primary key,
    code         text        not null,
    receiver     text        not null,
    tracing_code text        not null unique,
    type         varchar(16) not null,
    expires_at   timestamp   not null,
    unique (receiver, type)
);

create table if not exists otp_config
(
    type                   varchar(16) primary key,
    expire_time_seconds    integer not null default 60,
    char_count             integer not null default 6,
    include_alphabet_chars boolean not null default false,
    is_enabled             boolean not null default true,
    message_template       text    not null default '%s',
    check (char_count between 4 and 100)
);

insert into otp_config values ('EMAIL', 60, 8, true) on conflict do nothing;
insert into otp_config values ('SMS') on conflict do nothing;