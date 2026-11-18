ALTER TABLE profile_approval_request RENAME COLUMN updater TO applicator;
ALTER TABLE bank_account_approval_request RENAME COLUMN updater TO applicator;
ALTER TABLE bank_account RENAME COLUMN creator TO applicator;
ALTER TABLE profile RENAME COLUMN creator TO applicator;
ALTER TABLE profile_history RENAME COLUMN creator TO applicator;
ALTER TABLE company RENAME COLUMN creator TO applicator;
ALTER TABLE company_representative RENAME COLUMN creator TO applicator;

CREATE OR REPLACE FUNCTION triger_function() RETURNS TRIGGER AS
$BODY$
BEGIN
    INSERT INTO public.profile_history (original_data_id, change_request_date, change_request_type, email, user_id,
                                        create_date, identifier, address, first_name, last_name, mobile, telephone,
                                        nationality, gender, birth_date, status, postal_code, applicator, kyc_level, personal_identity_match, mobile_identity_match)
    VALUES (OLD.id, now(), 'UPDATE', OLD.email, OLD.user_id, OLD.create_date, OLD.identifier, OLD.address,
            OLD.first_name,
            OLD.last_name, OLD.mobile, OLD.telephone, OLD.nationality, OLD.gender, OLD.birth_date, OLD.status,
            OLD.postal_code, OLD.applicator, OLD.kyc_level, OLD.personal_identity_match, OLD.mobile_identity_match);
    RETURN NULL;
END;
$BODY$ language plpgsql;

CREATE OR REPLACE FUNCTION triger_delete_function() RETURNS TRIGGER AS
$BODY$
BEGIN
    INSERT INTO public.profile_history (original_data_id, change_request_date, change_request_type, email, user_id,
                                        create_date, identifier, address, first_name, last_name, mobile, telephone,
                                        nationality, gender, birth_date, status, postal_code, applicator, kyc_level, personal_identity_match, mobile_identity_match)
    VALUES (OLD.id, now(), 'DELETE', OLD.email, OLD.user_id, OLD.create_date, OLD.identifier, OLD.address,
            OLD.first_name,
            OLD.last_name, OLD.mobile, OLD.telephone, OLD.nationality, OLD.gender, OLD.birth_date, OLD.status,
            OLD.postal_code, OLD.applicator, OLD.kyc_level, OLD.personal_identity_match, OLD.mobile_identity_match);
    RETURN NULL;
END;
$BODY$ language plpgsql;
