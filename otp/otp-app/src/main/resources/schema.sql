create table if not exists otp
(
    id           serial primary key,
    code         text        not null,
    receiver     text        not null,
    tracing_code text        not null unique,
    type         varchar(16) not null,
    expires_at   timestamp   not null,
    request_date timestamp   not null default current_timestamp,
    is_verified  boolean     not null default false,
    is_active    boolean     not null default true,
    verify_time  timestamp
);

create table if not exists otp_config
(
    type                   varchar(16) primary key,
    expire_time_seconds    integer not null default 60,
    char_count             integer not null default 6,
    include_alphabet_chars boolean not null default false,
    is_enabled             boolean not null default true,
    is_activated           boolean not null default false,
    message_template       text    not null default '%s',
    check (char_count between 4 and 100)
);

create table if not exists totp
(
    id           serial primary key,
    user_id      text      not null unique,
    secret       text      not null unique,
    label        text,
    is_enabled   boolean   not null default true,
    is_activated boolean   not null default false,
    created_at   timestamp not null default current_timestamp
);

create table if not exists totp_config
(
    id           boolean primary key default true,
    secret_chars int  not null       default 64,
    issuer       text not null,
    constraint id check (id is true)
);

insert into otp_config
values ('EMAIL', 60, 8, true)
on conflict do nothing;

insert into otp_config
values ('SMS')
on conflict do nothing;

insert into otp_config
values ('COMPOSITE', 120)
on conflict do nothing;

insert into totp_config
values (true, 128, 'Opex')
on conflict do nothing;