alter table deposits
    add constraint transaction_ref_pk
        unique (transaction_ref);