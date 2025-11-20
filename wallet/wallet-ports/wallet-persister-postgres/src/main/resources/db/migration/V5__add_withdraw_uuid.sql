ALTER TABLE withdraws
    ADD COLUMN withdraw_uuid VARCHAR(255) NOT NULL default uuid_generate_v4();

ALTER TABLE withdraws_otp
    DROP CONSTRAINT withdraws_otp_withdraw_fkey;

ALTER TABLE withdraws_otp
    DROP CONSTRAINT withdraws_otp_withdraw_otp_type_key;

ALTER TABLE withdraws_otp
    RENAME COLUMN withdraw TO withdraw_uuid;

ALTER TABLE withdraws_otp
    ALTER COLUMN withdraw_uuid TYPE VARCHAR;

UPDATE withdraws_otp otp
SET withdraw_uuid = w.withdraw_uuid
FROM withdraws w
WHERE w.id = otp.withdraw_uuid::INTEGER;

ALTER TABLE public.withdraws_history
    ADD COLUMN withdraw_uuid VARCHAR(36);

CREATE OR REPLACE FUNCTION public.withdraws_update_history() RETURNS trigger AS
$$
BEGIN
    INSERT INTO public.withdraws_history (
        withdraw_id,
        uuid,
        withdraw_uuid,
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
        otp_required
    )
    VALUES (
               OLD.id,
               OLD.uuid,
               OLD.withdraw_uuid,
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
               OLD.otp_required
           );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


