Alter table chains drop column external_chain_scanner_url;

Alter table chains add column transaction_scanner_url VARCHAR(150);
Alter table chains add column address_scanner_url VARCHAR(150);
