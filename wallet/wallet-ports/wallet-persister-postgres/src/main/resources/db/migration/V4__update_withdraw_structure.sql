UPDATE withdraws SET status='ACCEPTED' WHERE status='PROCESSING';

CREATE TABLE IF NOT EXISTS withdraws_otp
(
    id               SERIAL PRIMARY KEY,
    withdraw         INTEGER     NOT NULL REFERENCES withdraws (id),
    otp_tracing_code TEXT        NOT NULL UNIQUE,
    otp_type         VARCHAR(20) NOT NULL,
    create_date      TIMESTAMP   NOT NULL,
    Unique (withdraw, otp_type)
);
CREATE INDEX idx_withdraws_otp_withdraw ON withdraws_otp (withdraw);

ALTER TABLE withdraws
    ADD COLUMN otp_required INTEGER NOT NULL DEFAULT 0;

ALTER TABLE withdraws
    ALTER COLUMN req_transaction_id DROP NOT NULL;

CREATE TABLE public.withdraws_history
(
    history_id           serial PRIMARY KEY,
    withdraw_id          integer NOT NULL,
    uuid                 varchar(36),
    req_transaction_id   varchar(20),
    final_transaction_id varchar(20),
    currency             varchar(20),
    wallet               integer,
    amount               numeric,
    applied_fee          numeric,
    dest_amount          numeric,
    dest_symbol          varchar(20),
    dest_network         varchar(80),
    dest_address         varchar(80),
    dest_notes           text,
    dest_transaction_ref varchar(100),
    description          text,
    status_reason        text,
    status               varchar(20),
    create_date          timestamp,
    last_update_date     timestamp,
    applicator           varchar(80),
    withdraw_type        varchar(255),
    attachment           varchar(255),
    transfer_method      varchar(255),
    otp_required         integer
);


CREATE OR REPLACE FUNCTION public.withdraws_update_history() RETURNS trigger AS
$$
BEGIN
    INSERT INTO public.withdraws_history (withdraw_id,
                                          uuid,
                                          req_transaction_id,
                                          final_transaction_id,
                                          currency,
                                          wallet,
                                          amount,
                                          applied_fee,
                                          dest_amount,
                                          dest_symbol,
                                          dest_network,
                                          dest_address,
                                          dest_notes,
                                          dest_transaction_ref,
                                          description,
                                          status_reason,
                                          status,
                                          create_date,
                                          last_update_date,
                                          applicator,
                                          withdraw_type,
                                          attachment,
                                          transfer_method,
                                          otp_required)
    VALUES (OLD.id,
            OLD.uuid,
            OLD.req_transaction_id,
            OLD.final_transaction_id,
            OLD.currency,
            OLD.wallet,
            OLD.amount,
            OLD.applied_fee,
            OLD.dest_amount,
            OLD.dest_symbol,
            OLD.dest_network,
            OLD.dest_address,
            OLD.dest_notes,
            OLD.dest_transaction_ref,
            OLD.description,
            OLD.status_reason,
            OLD.status,
            OLD.create_date,
            OLD.last_update_date,
            OLD.applicator,
            OLD.withdraw_type,
            OLD.attachment,
            OLD.transfer_method,
            OLD.otp_required);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_withdraws_update
    AFTER UPDATE
    ON public.withdraws
    FOR EACH ROW
EXECUTE FUNCTION public.withdraws_update_history();

